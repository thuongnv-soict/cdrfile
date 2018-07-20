/**
 * EntityActionServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package cdrfile.sms.service;

public class EntityActionServiceLocator extends org.apache.axis.client.Service implements EntityActionService {

    public EntityActionServiceLocator() {
    }


    public EntityActionServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public EntityActionServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for EntityAction
    private java.lang.String EntityAction_address = "http://10.3.11.136:9100/QueueSMS/services/EntityAction";

    public java.lang.String getEntityActionAddress() {
        return EntityAction_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String EntityActionWSDDServiceName = "EntityAction";

    public java.lang.String getEntityActionWSDDServiceName() {
        return EntityActionWSDDServiceName;
    }

    public void setEntityActionWSDDServiceName(java.lang.String name) {
        EntityActionWSDDServiceName = name;
    }

    public EntityAction getEntityAction() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(EntityAction_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getEntityAction(endpoint);
    }

    public EntityAction getEntityAction(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            EntityActionSoapBindingStub _stub = new EntityActionSoapBindingStub(portAddress, this);
            _stub.setPortName(getEntityActionWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setEntityActionEndpointAddress(java.lang.String address) {
        EntityAction_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (EntityAction.class.isAssignableFrom(serviceEndpointInterface)) {
                EntityActionSoapBindingStub _stub = new EntityActionSoapBindingStub(new java.net.URL(EntityAction_address), this);
                _stub.setPortName(getEntityActionWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("EntityAction".equals(inputPortName)) {
            return getEntityAction();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://service.queue", "EntityActionService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://service.queue", "EntityAction"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("EntityAction".equals(portName)) {
            setEntityActionEndpointAddress(address);
        }
        else
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
