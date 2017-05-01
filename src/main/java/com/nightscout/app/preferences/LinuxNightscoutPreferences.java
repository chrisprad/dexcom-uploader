package com.nightscout.app.preferences;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.nightscout.core.model.GlucoseUnit;
import com.nightscout.core.preferences.NightscoutPreferences;

public class LinuxNightscoutPreferences implements NightscoutPreferences {

	protected boolean restApiEnabled;
	protected List<String> restApiBaseUris;
	protected boolean calibrationUploadEnabled;
	protected boolean sensorUploadEnabled;
	protected boolean mongoUploadEnabled;
	protected boolean dataDonateEnabled;
	protected String mongoClientUri;
	protected String mongoCollection;
	protected String mongoDeviceStatusCollection;
	protected boolean iUnderstand;
	protected GlucoseUnit preferredUnits;
	protected String pwdName;
	protected boolean AskedForData;
	protected String configurationFileName;
	
	public LinuxNightscoutPreferences() {
		
	}
	public LinuxNightscoutPreferences(String configurationFileName) throws IOException {
		this.configurationFileName = configurationFileName;
		LoadConfigurationFile();
	}
	
	@Override
	public boolean isRestApiEnabled() {
		return restApiEnabled;
	}

	@Override
	public void setRestApiEnabled(boolean restApiEnabled) {
		this.restApiEnabled = restApiEnabled;
	}

	@Override
	public List<String> getRestApiBaseUris() {
		return restApiBaseUris;
	}

	@Override
	public void setRestApiBaseUris(List<String> restApis) {
		restApiBaseUris = restApis;
	}

	@Override
	public boolean isCalibrationUploadEnabled() {
		return calibrationUploadEnabled;
	}

	@Override
	public void setCalibrationUploadEnabled(boolean calibrationUploadEnabled) {
		this.calibrationUploadEnabled = calibrationUploadEnabled; 
	}

	@Override
	public boolean isSensorUploadEnabled() {
		return sensorUploadEnabled;
	}

	@Override
	public void setSensorUploadEnabled(boolean sensorUploadEnabled) {
		this.sensorUploadEnabled = sensorUploadEnabled;
	}

	@Override
	public boolean isMongoUploadEnabled() {
		return mongoUploadEnabled;
	}

	@Override
	public void setMongoUploadEnabled(boolean mongoUploadEnabled) {
		this.mongoUploadEnabled = mongoUploadEnabled;
	}

	@Override
	public boolean isDataDonateEnabled() {
		return dataDonateEnabled;
	}

	@Override
	public void setDataDonateEnabled(boolean toDonate) {
		dataDonateEnabled = toDonate;
	}

	@Override
	public String getMongoClientUri() {
		return mongoClientUri;
	}

	@Override
	public void setMongoClientUri(String client) {
		mongoClientUri = client;
	}

	@Override
	public String getMongoCollection() {
		return mongoCollection;
	}

	@Override
	public void setMongoCollection(String mongoCollection) {
		this.mongoCollection = mongoCollection;
	}

	@Override
	public String getMongoDeviceStatusCollection() {
		return mongoDeviceStatusCollection;
	}

	@Override
	public void setMongoDeviceStatusCollection(String deviceStatusCollection) {
		this.mongoDeviceStatusCollection = deviceStatusCollection;
	}

	@Override
	public boolean getIUnderstand() {
		return iUnderstand;
	}

	@Override
	public void setIUnderstand(boolean bool) {
		this.iUnderstand = bool;
	}

	@Override
	public GlucoseUnit getPreferredUnits() {
		return preferredUnits;
	}

	@Override
	public void setPreferredUnits(GlucoseUnit units) {
		this.preferredUnits = units;
	}

	@Override
	public String getPwdName() {
		return pwdName;
	}

	@Override
	public void setPwdName(String pwdName) {
		this.pwdName = pwdName;
	}

	@Override
	public boolean hasAskedForData() {
		return AskedForData;
	}

	@Override
	public void setAskedForData(boolean askedForData) {
		this.AskedForData = askedForData;
	}
	
	public void LoadConfigurationFile() throws IOException {
		File configurationFile = new File(configurationFileName);
		FileReader configurationReader = new FileReader(configurationFile);
		Properties props = new Properties();
		props.load(configurationReader);
		LoadProperties(props);
		configurationReader.close();
	}
	public void SaveConfigurationFile() throws IOException {
		SaveConfigurationFile(this.configurationFileName);
	}
	public void SaveConfigurationFile(String configurationFileName) throws IOException {
		File configurationFile = new File(configurationFileName);
		FileWriter writer = new FileWriter(configurationFile);
		Properties props = new Properties();
		SaveProperties(props);
		props.store(writer,"dexcom-uploader configuration file");
		writer.close();
	}
	
	protected void LoadProperties(Properties props) {
		this.restApiEnabled = Boolean.parseBoolean(props.getProperty("restApiEnabled", "false"));
		List<String> restUriList = Arrays.asList(props.getProperty("restApiBaseUris", "false").split(","));
		this.restApiBaseUris = restUriList;
		this.calibrationUploadEnabled = Boolean.parseBoolean(props.getProperty("calibrationUploadEnabled", "false"));
		this.sensorUploadEnabled = Boolean.parseBoolean(props.getProperty("sensorUploadEnabled", "false"));
		this.mongoUploadEnabled = Boolean.parseBoolean(props.getProperty("mongoUploadEnabled", "false"));
		this.dataDonateEnabled = Boolean.parseBoolean(props.getProperty("dataDonateEnabled", "false"));
		this.mongoClientUri = props.getProperty("mongoClientUri", "");
		this.mongoCollection = props.getProperty("mongoCollection", "");
		this.mongoDeviceStatusCollection = props.getProperty("mongoDeviceStatusCollection", "");
		this.iUnderstand = Boolean.parseBoolean(props.getProperty("iUnderstand", "false"));
		this.preferredUnits = GlucoseUnit.valueOf(props.getProperty("preferredUnits", "MGDL"));
		this.pwdName = props.getProperty("pwdName", "");
		this.AskedForData = Boolean.parseBoolean(props.getProperty("AskedForData", "false"));

	}
	protected void SaveProperties(Properties props) {
		props.setProperty("restApiEnabled", Boolean.toString(restApiEnabled));
		final StringBuilder restBuilder = new StringBuilder();
		for(String apiUri : restApiBaseUris) {
			restBuilder.append(apiUri).append(",");
		}
		//remove trailing ,
		restBuilder.deleteCharAt(restBuilder.length() - 1);
		props.setProperty("restApiBaseUris", restBuilder.toString());
		props.setProperty("calibrationUploadEnabled", Boolean.toString(calibrationUploadEnabled));
		props.setProperty("sensorUploadEnabled", Boolean.toString(sensorUploadEnabled));
		props.setProperty("mongoUploadEnabled", Boolean.toString(mongoUploadEnabled));
		props.setProperty("dataDonateEnabled", Boolean.toString(dataDonateEnabled));
		props.setProperty("mongoClientUri", (mongoClientUri != null) ? mongoClientUri : "");
		props.setProperty("mongoCollection",(mongoCollection != null) ? mongoCollection : "");
		props.setProperty("mongoDeviceStatusCollection", (mongoDeviceStatusCollection != null) ? mongoDeviceStatusCollection : "");
		props.setProperty("iUnderstand", Boolean.toString(iUnderstand));
		props.setProperty("preferredUnits", preferredUnits.toString());
		props.setProperty("pwdName", (pwdName != null) ? pwdName : "");
		props.setProperty("AskedForData", Boolean.toString(AskedForData));
	}
}
