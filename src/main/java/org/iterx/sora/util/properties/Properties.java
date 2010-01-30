package org.iterx.sora.util.properties;


//TODO: LDAP style directory/properties hierarchy
public interface Properties {

    String[] getPropertyNames();

    String getProperty(String name);
 
}
