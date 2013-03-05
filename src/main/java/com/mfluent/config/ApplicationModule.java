package com.mfluent.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTablePool;
import org.springframework.context.annotation.Bean;

import com.mfluent.data.UserDAO;


/**
 * // Declare "application" scope beans here (ie., beans that are not only used by the web context)
 *
 */
@org.springframework.context.annotation.Configuration
public class ApplicationModule
{
	
	@Bean
    public Configuration hbaseConfiguration() {
		System.err.println("********** ApplicationModule::dataSource");
		Configuration configuration = HBaseConfiguration.create();
		return configuration;

    }
	
	@Bean
	public HTablePool htablePool() {
		System.err.println("********** ApplicationModule::getHTablePool");
		return new HTablePool(hbaseConfiguration(), 10);
	}
	
	@Bean
	public UserDAO userDAO() {
		System.err.println("********** ApplicationModule::userDao");
		return new UserDAO();
	}
}
