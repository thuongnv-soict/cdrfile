/**
 * EntityAction.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package cdrfile.sms.service;

public interface EntityAction extends java.rmi.Remote {
    public void main(java.lang.String[] arg) throws java.rmi.RemoteException;
    public java.lang.String sendSMSAlert(java.lang.String strISDN, java.lang.String strSystem, java.lang.String strContents) throws java.rmi.RemoteException;
}
