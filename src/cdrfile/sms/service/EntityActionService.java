/**
 * EntityActionService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package cdrfile.sms.service;

public interface EntityActionService extends javax.xml.rpc.Service {
    public java.lang.String getEntityActionAddress();

    public EntityAction getEntityAction() throws javax.xml.rpc.ServiceException;

    public EntityAction getEntityAction(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
