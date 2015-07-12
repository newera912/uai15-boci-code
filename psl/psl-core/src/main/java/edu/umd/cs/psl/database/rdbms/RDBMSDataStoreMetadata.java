/*
 * This file is part of the PSL software.
 * Copyright 2011-2013 University of Maryland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.cs.psl.database.rdbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import edu.umd.cs.psl.database.DataStore;
import edu.umd.cs.psl.database.DataStoreMetdata;
import edu.umd.cs.psl.database.Partition;
import edu.umd.cs.psl.database.rdbms.RDBMSPartition;

/**
 * @author jay
 *
 */
public class RDBMSDataStoreMetadata implements DataStoreMetdata {
	private static final Logger log = LoggerFactory.getLogger(RDBMSDataStoreMetadata.class);
	private String mdTableName;
	private Connection conn;
	private HashMap<String,Integer> partitionNames;
	private boolean mdTableExists = false;
	
	public RDBMSDataStoreMetadata(Connection conn, String mdTableName){
		this.mdTableName = mdTableName;
		this.conn = conn;
		partitionNames = new HashMap<String, Integer>();	
	}
	
	protected Connection getConnection(){
		return conn;
	}
	
	protected String getMetadataTableName() {
		return mdTableName;
	}
	
	public boolean checkIfMetadataTableExists(){
		if(mdTableExists){ return mdTableExists; }
		boolean exists = false;
		// This should work for MySQL and H2, but not sure about other things (like Oracle)
		try {
			ResultSet rs = conn.getMetaData().getTables(null, null, mdTableName, null);
			if(rs.next()) { exists = true; mdTableExists=true;}
		} catch (Exception e) {
			log.error("Could not determine if metdata table exists - "+e.getMessage());
		};	
		return exists;
	}
	
	public void createMetadataTable(){
		if(checkIfMetadataTableExists())
			return;
		try {
			PreparedStatement stmt = conn.prepareStatement("CREATE TABLE "+mdTableName+" (namespace VARCHAR(20), keytype VARCHAR(20), key VARCHAR(255), value VARCHAR(255), PRIMARY KEY(namespace,keytype,key))");
			stmt.execute();
		} catch (Exception e) { 
			log.error("Error while creating metadata table - "+e.getMessage());
		}
		
	}
	
	
	/**** Database Helper functions ****/
	protected boolean addRow(String mdTableName, String space, String type, String key, String val){
		try{
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO "+mdTableName+" VALUES(?, ?, ?, ?)");
			stmt.setString(1, space);
			stmt.setString(2, type);
			stmt.setString(3, key);
			stmt.setString(4, val);
			stmt.execute();						
		} catch (Exception e) {
			log.info("Error adding row to metadata - "+e.getMessage());
			return false;
		}
		return true;
	}

	protected String getValue(String mdTableName, String space, String type, String key){
		try{
			PreparedStatement stmt = conn.prepareStatement("SELECT value from "+mdTableName+" WHERE namespace = ? AND keytype = ? AND key = ?");
			stmt.setString(1, space);
			stmt.setString(2, type);
			stmt.setString(3, key);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			if(rs.next()){
				return rs.getString(1);
			}
		} catch (Exception e) {
			log.info("Error getting value for key "+key+" - "+e.getMessage());
			return null;
		}
		return null;
	}
	
	
	protected boolean removeRow(String mdTableName, String space, String type, String key) {
		try{
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM "+mdTableName+" WHERE namespace = ? AND keytype = ? AND key = ?");
			stmt.setString(1, space);
			stmt.setString(2, type);
			stmt.setString(3, key);
			stmt.execute();						
		} catch (Exception e) {
			log.info("Error removing row - "+e.getMessage());
			return false;
		}
		return true;	
	}

	public Map<String,String> getAllValuesByType(String mdTableName, String space, String type){
		Map<String, String> vals = null;
		try{
			PreparedStatement stmt = conn.prepareStatement("SELECT (key,value) from "+mdTableName+" WHERE namespace = ? AND keytype = ?");
			stmt.setString(1, space);
			stmt.setString(2, type);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			vals = new HashMap<String,String>();
			while(rs.next()){
				vals.put(rs.getString(1), rs.getString(2));
			}
		} catch (Exception e) {
			log.error("Error getting all values for type "+type+e.getMessage());
		}
		return vals;
	}
	
	
	

	
	/**** Partition-related functions ****/
	public void loadPartitionNames(){
		Map<String,String> vals = getAllValuesByType(mdTableName,"Partition","name");
		if(vals != null){
			for(String name : vals.keySet()){
				int id = Integer.parseInt(vals.get(name));				
				partitionNames.put(name, id);
			}
		}
	}
	
	public Partition getPartitionByName(String name){
		Partition p = null;
		String idStr = getValue(mdTableName,"Partition","name",name);
		if(idStr != null){
			p = new RDBMSPartition(Integer.parseInt(idStr),name);
		}
		return p;
	}
	
	public Set<Partition> getAllPartitions(){
		Set<Partition> partitions = null; 
		Map<String,String> vals = getAllValuesByType(mdTableName,"Partition","name");
		if(vals != null){
			partitions = new HashSet<Partition>();
			for(String name : vals.keySet()){
				int id = Integer.parseInt(vals.get(name));				
				partitions.add(new RDBMSPartition(id,name));
			}
		}
		return partitions;
	}
	
	public int getMaxPartition(){
		int max = 0;
		try{
			PreparedStatement stmt = conn.prepareStatement("SELECT MAX(CAST(value as INT)) from "+mdTableName+" WHERE namespace = 'Partition' AND keytype = 'name'");
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			if(rs.next()){
				max = Integer.parseInt(rs.getString(1));
			}
		} catch (Exception e) {
			log.error("Could not get max partition - "+e.getMessage());
			return 0;
		}		
		return max;
	}
	
	/* 
	 */
	@Override
	public boolean addPartition(Partition p) {
		boolean success = false;
		if(getPartitionByName(p.getName())!=null){
			log.error("Partition named"+p.getName()+" already exists");
		} else {
			success = addRow(mdTableName,"Partition","name",p.getName(),Integer.toString(p.getID()));
			if(success)
					partitionNames.put(p.getName(), p.getID());
		}
		return success;
	}

	/* 	 */
	@Override
	public boolean removePartition(Partition p) {
		boolean success = false;
		success = removeRow(mdTableName,"Partition","name",p.getName());
		if(success)
			partitionNames.remove(p.getName());
		return success;
	}

}
