package com.mfluent.data;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;

import com.mfluent.data.hbase.UserTable;
import com.mfluent.data.model.User;

public class UserDAO {

	private static final Log LOG = LogFactory.getLog(UserDAO.class);

	Configuration hbaseConfiguration;
	
	HTablePool htablePool;
	
	public void createUserTableIfNotExist() throws IOException {
		HBaseAdmin admin = null;
		try {
			admin =  new HBaseAdmin(hbaseConfiguration);;
			
			if (admin.tableExists(UserTable.NAME) == false) {
				HTableDescriptor tableDescriptor = new HTableDescriptor(UserTable.NAME);
				HColumnDescriptor family = new HColumnDescriptor(UserTable.DATA_FAMILY);
				tableDescriptor.addFamily(family);
				admin.createTable(tableDescriptor);
				
				if (admin.tableExists(UserTable.NAME) == false) {
					if (LOG.isErrorEnabled()) {
						LOG.error("Failed to create UserTable");
					}
					throw new RuntimeException("Failed to create UserTable");
				}
				
			}
		} catch (Exception e) {
			if (LOG.isFatalEnabled()) {
				LOG.fatal("Failed to create UserTable", e);
			}
		} finally {
			if (admin != null) {
				admin.close();
			}
		}
	}
	
	/**
	 * Creates a user in the user table.
	 * 
	 * @param username
	 *            The username to use.
	 * @param firstName
	 *            The first name of the user.
	 * @param lastName
	 *            The last name of the user.
	 * @param email
	 *            The email address of the user.
	 * @param password
	 *            The password of the user.
	 * @param roles
	 *            The user roles assigned to the new user.
	 * @throws IOException
	 *             When adding the user fails.
	 */
	public void createUser(String username, String firstName, String lastName, String email, String password, String roles) throws IOException {
		HTableInterface table = htablePool.getTable(UserTable.NAME);
		try {
			Put put = new Put(Bytes.toBytes(username));
			put.add(UserTable.DATA_FAMILY, UserTable.FIRSTNAME, Bytes.toBytes(firstName));
			put.add(UserTable.DATA_FAMILY, UserTable.LASTNAME, Bytes.toBytes(lastName));
			put.add(UserTable.DATA_FAMILY, UserTable.EMAIL, Bytes.toBytes(email));
			put.add(UserTable.DATA_FAMILY, UserTable.CREDENTIALS, Bytes.toBytes(password));
			put.add(UserTable.DATA_FAMILY, UserTable.ROLES, Bytes.toBytes(roles));
	
			table.put(put);
			table.flushCommits();
		} finally {
			table.close();
		}
	  }
	
	public User getUser(String username) throws IOException {
		User user = null;
		HTableInterface table = null;
		try {
			table = htablePool.getTable(UserTable.NAME);

			Get get = new Get(Bytes.toBytes(username.toLowerCase()));

			Result result = table.get(get);
			if (result.isEmpty()) {
				return null;
			}

			String firstName = Bytes.toString(result.getValue(UserTable.DATA_FAMILY, UserTable.FIRSTNAME));
			String lastName = Bytes.toString(result.getValue(UserTable.DATA_FAMILY, UserTable.LASTNAME));
			String email = Bytes.toString(result.getValue(UserTable.DATA_FAMILY, UserTable.EMAIL));
			String credentials = Bytes.toString(result.getValue(UserTable.DATA_FAMILY, UserTable.CREDENTIALS));
			String roles = Bytes.toString(result.getValue(UserTable.DATA_FAMILY, UserTable.ROLES));
			user = new User(username, firstName, lastName, email, credentials, roles);
		} catch (Exception e) {
			LOG.error(String.format("Unable to get user '%s'", username), e);
		} finally {
			if (table != null) {
				table.close();
			}
		}

		return user;
	}
	
	
	@Autowired
	public void setHbaseConfiguration(Configuration hbaseConfiguration) {
		this.hbaseConfiguration = hbaseConfiguration;
	}
	
	@Autowired
	public void setHTablePool(HTablePool htablePool) {
		this.htablePool = htablePool;
	}
}
