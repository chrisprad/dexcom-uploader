package com.nightscout.app.upload;

import com.mongodb.MongoClientURI;
import com.nightscout.app.drivers.LinuxUploaderDevice;
//import com.nightscout.android.drivers.AndroidUploaderDevice;
import com.nightscout.core.dexcom.Utils;
import com.nightscout.core.dexcom.records.GlucoseDataSet;
import com.nightscout.core.drivers.AbstractUploaderDevice;
import com.nightscout.core.model.CalibrationEntry;
import com.nightscout.core.model.DownloadResults;
import com.nightscout.core.model.G4Download;
import com.nightscout.core.model.MeterEntry;
import com.nightscout.core.model.SensorEntry;
import com.nightscout.core.model.SensorGlucoseValueEntry;
import com.nightscout.core.preferences.NightscoutPreferences;
import com.nightscout.core.upload.BaseUploader;
import com.nightscout.core.upload.MongoUploader;
import com.nightscout.core.upload.RestLegacyUploader;
import com.nightscout.core.upload.RestV1Uploader;
import com.squareup.wire.Message;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class Uploader {
    private static final String LOG_TAG = Uploader.class.getSimpleName();
    private final List<BaseUploader> uploaders;
    private boolean allUploadersInitalized = true;
    static final Logger Log = LoggerFactory.getLogger(Uploader.class);
    
    public Uploader(NightscoutPreferences preferences) {
        uploaders = new ArrayList<>();
        if (preferences.isMongoUploadEnabled()) {
            allUploadersInitalized &= initializeMongoUploader(preferences);
        }
        if (preferences.isRestApiEnabled()) {
            allUploadersInitalized &= initializeRestUploaders(preferences);
        }
    }

    private boolean initializeMongoUploader(NightscoutPreferences preferences) {
        String dbURI = preferences.getMongoClientUri();
        String collectionName = preferences.getMongoCollection();
        String dsCollectionName = preferences.getMongoDeviceStatusCollection();
        checkNotNull(collectionName);
        checkNotNull(dsCollectionName);
        MongoClientURI uri;
        try {
            uri = new MongoClientURI(dbURI);
        } catch (IllegalArgumentException e) {
            Log.error(LOG_TAG, "Error creating mongo client uri for " + dbURI + ".", e);
            return false;
        } catch (NullPointerException e) {
            Log.error(LOG_TAG, "Error creating mongo client uri for null value.", e);
            return false;
        }
        uploaders.add(new MongoUploader(preferences, uri, collectionName, dsCollectionName));
        return true;
    }

    private boolean initializeRestUploaders(NightscoutPreferences preferences) {
        List<String> baseUrisSetting = preferences.getRestApiBaseUris();
        List<URI> baseUris = new ArrayList<>();
        boolean allInitialized = true;
        for (String baseURLSetting : baseUrisSetting) {
            String baseUriString = baseURLSetting.trim();
            if (baseUriString.isEmpty()) continue;
            try {
                baseUris.add(URI.create(baseUriString));
            } catch (IllegalArgumentException e) {
                Log.error(LOG_TAG, "Error creating rest uri from preferences.", e);
            }
        }

        for (URI baseUri : baseUris) {
            if (baseUri.getPath().contains("v1")) {
                try {
                    uploaders.add(new RestV1Uploader(preferences, baseUri));
                } catch (IllegalArgumentException e) {
                    Log.error(LOG_TAG, "Error initializing rest v1 uploader.", e);
                    allInitialized &= false;
                }
            } else {
                uploaders.add(new RestLegacyUploader(preferences, baseUri));
            }
        }
        return allInitialized;
    }

    public boolean upload(DownloadResults downloadResults, int numRecords) {
        G4Download download = downloadResults.getDownload();
        List<SensorGlucoseValueEntry> sgvList = filterRecords(numRecords, download.sgv);
        List<CalibrationEntry> calList = filterRecords(numRecords, download.cal);
        List<MeterEntry> meterList = filterRecords(numRecords, download.meter);
        List<SensorEntry> sensorList = filterRecords(numRecords, download.sensor);

        List<GlucoseDataSet> glucoseDataSets = Utils.mergeGlucoseDataRecords(sgvList, sensorList);

        return upload(glucoseDataSets, meterList, calList);
    }

    private <T extends Message> List<T> filterRecords(int numRecords, List<T> records) {
        int recordIndexToStop = Math.max(records.size() - numRecords, 0);
        List<T> results = new ArrayList<>();
        for (int i = records.size(); i > recordIndexToStop; i--) {
            results.add(records.get(i - 1));
        }
        return results;
    }

    public boolean upload(DownloadResults downloadResults) {
        G4Download download = downloadResults.getDownload();
        List<GlucoseDataSet> glucoseDataSets = Utils.mergeGlucoseDataRecords(download.sgv, download.sensor);
        return upload(glucoseDataSets, download.meter, download.cal);
    }

    private boolean upload(List<GlucoseDataSet> glucoseDataSets,
                           List<MeterEntry> meterRecords,
                           List<CalibrationEntry> calRecords) {

        AbstractUploaderDevice deviceStatus = LinuxUploaderDevice.getUploaderDevice();

        boolean allSuccessful = true;
        for (BaseUploader uploader : uploaders) {
            // TODO(klee): capture any exceptions here so that all configured uploaders will attempt
            // to upload
            allSuccessful &= uploader.uploadRecords(glucoseDataSets, meterRecords, calRecords, deviceStatus);
        }

        // Force a failure if an uploader was not properly initialized, but only after the other
        // uploaders were executed.
        return allUploadersInitalized && allSuccessful && (uploaders.size() != 0);
    }

    protected List<BaseUploader> getUploaders() {
        return uploaders;
    }

    protected boolean areAllUploadersInitialized() {
        return allUploadersInitalized;
    }
}
