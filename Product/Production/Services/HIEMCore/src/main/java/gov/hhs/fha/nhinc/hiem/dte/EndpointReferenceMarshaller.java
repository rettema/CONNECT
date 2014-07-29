/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.hiem.dte;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.w3._2005._08.addressing.EndpointReferenceType;
import org.w3._2005._08.addressing.ObjectFactory;
import org.w3c.dom.Element;

/**
 *
 * @author rayj
 * @author richard.ettema
 */
public class EndpointReferenceMarshaller {

    private static final String EndpointReferenceContextPath = "org.w3._2005._08.addressing";

    public static String marshalString(EndpointReferenceType dataObject) {
        try {
            String xmlString = "";
            JAXBContext context = JAXBContext.newInstance(EndpointReferenceType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(dataObject, stringWriter);
            xmlString = stringWriter.toString();
            return xmlString;
        } catch(JAXBException jaxbEx) {
            throw new RuntimeException(jaxbEx);
        }
    }

    public static Element marshal(EndpointReferenceType object) {

        ObjectFactory addrObjFact = new ObjectFactory();
        JAXBElement<EndpointReferenceType> jaxb = addrObjFact.createEndpointReference(object);

        MarshallerHelper marshaller = new MarshallerHelper();

        Element element = marshaller.marshal(jaxb, EndpointReferenceContextPath);

        return element;
    }

    @SuppressWarnings("rawtypes")
    public EndpointReferenceType unmarshal(Element element) {

        EndpointReferenceType endpointReference = null;
        MarshallerHelper marshaller = new MarshallerHelper();
        Object object = marshaller.unmarshal(element, EndpointReferenceContextPath);

        if (object instanceof EndpointReferenceType) {
            endpointReference = (EndpointReferenceType) object;

        } else if (object instanceof JAXBElement) {
            JAXBElement jaxb = (JAXBElement) object;
            Object jaxbValue = jaxb.getValue();

            if (jaxbValue instanceof EndpointReferenceType) {
                endpointReference = (EndpointReferenceType) object;
            }
        }

        return endpointReference;
    }

}
