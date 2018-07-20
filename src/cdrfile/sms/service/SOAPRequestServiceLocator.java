/**
 * SOAPRequestServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package cdrfile.sms.service;

public class SOAPRequestServiceLocator extends org.apache.axis.client.Service implements SOAPRequestService {

    public SOAPRequestServiceLocator() {
    }


    public SOAPRequestServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SOAPRequestServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SOAPRequestPort
    private java.lang.String SOAPRequestPort_address = "http://10.50.9.91:8080/soap";

    public java.lang.String getSOAPRequestPortAddress() {
        return SOAPRequestPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SOAPRequestPortWSDDServiceName = "SOAPRequestPort";

    public java.lang.String getSOAPRequestPortWSDDServiceName() {
        return SOAPRequestPortWSDDServiceName;
    }

    public void setSOAPRequestPortWSDDServiceName(java.lang.String name) {
        SOAPRequestPortWSDDServiceName = name;
    }

    public SOAPRequest getSOAPRequestPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SOAPRequestPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSOAPRequestPort(endpoint);
    }

    public SOAPRequest getSOAPRequestPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            SOAPRequestPortBindingStub _stub = new SOAPRequestPortBindingStub(portAddress, this);
            _stub.setPortName(getSOAPRequestPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSOAPRequestPortEndpointAddress(java.lang.String address) {
        SOAPRequestPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (SOAPRequest.class.isAssignableFrom(serviceEndpointInterface)) {
                SOAPRequestPortBindingStub _stub = new SOAPRequestPortBindingStub(new java.net.URL(SOAPRequestPort_address), this);
                _stub.setPortName(getSOAPRequestPortWSDDServiceName());
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
        if ("SOAPRequestPort".equals(inputPortName)) {
            return getSOAPRequestPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://object.app.telsoft/", "SOAPRequestService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://object.app.telsoft/", "SOAPRequestPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("SOAPRequestPort".equals(portName)) {
            setSOAPRequestPortEndpointAddress(address);
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
