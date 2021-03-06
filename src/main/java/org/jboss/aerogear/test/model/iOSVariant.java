/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.test.model;

/**
 * The iOS variant class encapsulates APNs specific behavior.
 */
public class iOSVariant extends AbstractVariant {
    private static final long serialVersionUID = -889367404039436329L;

    public iOSVariant() {
        super();
    }

    private boolean production;

    private String passphrase;

    private byte[] certificate;

    @Override
    public VariantType getType() {
        return VariantType.IOS;
    }

    /**
     * If <code>true</code> a connection to Apple's Production APNs server
     * will be established for this iOS variant.
     *
     * If the method returns <code>false</code> a connection to
     * Apple's Sandbox/Development APNs server will be established
     * for this iOS variant.
     */
    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

    /**
     * The APNs passphrase that is needed to establish a connection to any
     * of Apple's APNs Push Servers.
     */
    public String getPassphrase() {
        return this.passphrase;
    }

    public void setPassphrase(final String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * The APNs certificate that is needed to establish a connection to any
     * of Apple's APNs Push Servers.
     */
    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] cert) {
        this.certificate = cert;
    }
}
