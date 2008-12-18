/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <embUnit.h>
#include <stdlib.h>
#include <string.h>
#include "wbxml_parser.h"

#define DEFINE_TEST(test)   \
    new_TestFixture(#test, test)

#define ASSERT_EQUAL_INT(expected, actual) \
{   \
    int tmp = actual;   /* avoid duplicated evaluation in TEST_ASSERT_EQUAL_INT */  \
    TEST_ASSERT_EQUAL_INT(expected, tmp);   \
}

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1"></WV-CSP-Message>
 */
static const char simple_wbxml[] = {
    0x03, 0x10, 0x6a, 0x00, 0xc9, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01, 0x01
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1" />
 */
static const char simple_nocontent_wbxml[] = {
      0x03, 0x10, 0x6a, 0x00, 0x89, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1">
    <Session></Session>
</WV-CSP-Message>
*/
static const char simple2_wbxml[] = {
    0x03, 0x10, 0x6a, 0x00, 0xc9, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01,
    0x6d, 0x01, 0x01
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1">
    <Session>
        <SessionDescriptor>
            <SessionType>Outband</SessionType>
        </SessionDescriptor>
    </Session>
</WV-CSP-Message>
*/
static const char simple3_wbxml[] = {
    0x03, 0x10, 0x6a, 0x00, 0xc9, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01,
    0x6d, 0x6e, 0x70, 0x80, 0x19, 0x01, 0x01, 0x01, 0x01
};

/* A STR_I (the '0x03' after '0x70') without ending '\0' */
static const char incomplete_str_i_wbxml[] = {
    0x03, 0x10, 0x6a, 0x00, 0xc9, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01,
    0x6d, 0x6e, 0x70, 0x03, 0x44, 0x55, 0x66, 0x01, 0x01, 0x01, 0x01
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1">
    <Session>
        <SessionDescriptor>
            <SessionType>&#1114111</SessionType>
        </SessionDescriptor>
    </Session>
</WV-CSP-Message>
*/
/* &#1114111 = U+10ffff = mb_u_int32 C3 ff 7f */
static const char big_entity_wbxml[] = {
    0x03, 0x10, 0x6a, 0x00, 0xc9, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01,
    0x6d, 0x6e, 0x70, 0x02, 0xc3, 0xff, 0x7f, 0x01, 0x01, 0x01, 0x01
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1">
    <Session>
        <SessionDescriptor>
            <SessionType>&#1114112</SessionType>
        </SessionDescriptor>
    </Session>
</WV-CSP-Message>
*/
/* &#1114112 = 0x110000 = mb_u_int32 C4 80 00 */
static const char invalid_entity_wbxml[] = {
    0x03, 0x10, 0x6a, 0x00, 0xc9, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01,
    0x6d, 0x6e, 0x70, 0x02, 0xc4, 0x80, 0x00, 0x01, 0x01, 0x01, 0x01
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1">
    <Session>
        <SessionDescriptor>
            <SessionType>Outband</SessionType>
        </SessionDescriptor>
        <Transaction>
            <TransactionDescriptor>
                <TransactionMode>Response</TransactionMode>
                <TransactionID>transId1</TransactionID>
            </TransactionDescriptor>
            <TransactionContent xmlns="http://www.wireless-village.org/TRC1.1">
                <Login-Response>
                    <ClientID>Esmertec112233</ClientID>
                    <Result>
                        <Code>401</Code>
                        <Description>Please complete authentication challenge</Description>
                    </Result>
                    <Nonce>e6b68ebd81309d538357e39e2fba3204</Nonce>
                    <DigestSchema>MD5</DigestSchema>
                    <CapabilityRequest>F</CapabilityRequest>
                </Login-Response>
            </TransactionContent>
        </Transaction>
    </Session>
</WV-CSP-Message>
*/
static const char loginresponse_wbxml[] = {
    0x03, 0x10, 0x6a, 0x00, 0xc9, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01,
    0x6d, 0x6e, 0x70, 0x80, 0x19, 0x01, 0x01, 0x72, 0x74, 0x76, 0x80, 0x21,
    0x01, 0x75, 0x03, 0x74, 0x72, 0x61, 0x6e, 0x73, 0x49, 0x64, 0x31, 0x00,
    0x01, 0x01, 0xf3, 0x07, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01, 0x00, 0x01,
    0x5e, 0x00, 0x00, 0x4a, 0x03, 0x45, 0x73, 0x6d, 0x65, 0x72, 0x74, 0x65,
    0x63, 0x31, 0x31, 0x32, 0x32, 0x33, 0x33, 0x00, 0x01, 0x6a, 0x4b, 0x03,
    0x34, 0x30, 0x31, 0x00, 0x01, 0x52, 0x03, 0x50, 0x6c, 0x65, 0x61, 0x73,
    0x65, 0x20, 0x63, 0x6f, 0x6d, 0x70, 0x6c, 0x65, 0x74, 0x65, 0x20, 0x61,
    0x75, 0x74, 0x68, 0x65, 0x6e, 0x74, 0x69, 0x63, 0x61, 0x74, 0x69, 0x6f,
    0x6e, 0x20, 0x63, 0x68, 0x61, 0x6c, 0x6c, 0x65, 0x6e, 0x67, 0x65, 0x00,
    0x01, 0x01, 0x00, 0x01, 0x60, 0x03, 0x65, 0x36, 0x62, 0x36, 0x38, 0x65,
    0x62, 0x64, 0x38, 0x31, 0x33, 0x30, 0x39, 0x64, 0x35, 0x33, 0x38, 0x33,
    0x35, 0x37, 0x65, 0x33, 0x39, 0x65, 0x32, 0x66, 0x62, 0x61, 0x33, 0x32,
    0x30, 0x34, 0x00, 0x01, 0x4f, 0x03, 0x4d, 0x44, 0x35, 0x00, 0x01, 0x4b,
    0x80, 0x0b, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.openmobilealliance.org/DTD/IMPS-CSP1.3">
 <Session>
  <SessionDescriptor>
   <SessionType>Inband</SessionType>
   <SessionID>im.user.com#48815@server.com</SessionID>
  </SessionDescriptor>
  <Transaction>
   <TransactionDescriptor>
    <TransactionMode>Response</TransactionMode>
    <TransactionID>IMApp01#12345@NOK5110</TransactionID>
   </TransactionDescriptor>
   <TransactionContent xmlns="http://www.openmobilealliance.org/DTD/IMPS-TRC1.3">
    <Status>
     <Result>
      <Code>201</Code>
      <Description>Partially successful.</Description>
      <DetailedResult>
       <Code>531</Code>
       <Description>Unknown user.</Description>
       <UserID>wv:bad_user1@im.com</UserID>
       <UserID>wv:bad_user2@im.com</UserID>
      </DetailedResult>
      <DetailedResult>
       <Code>532</Code>
       <Description>Blocked.</Description>
       <UserID>wv:bad_user3@im.com</UserID>
       <UserID>wv:bad_user4@im.com</UserID>
      </DetailedResult>
     </Result>
    </Status>
   </TransactionContent>
  </Transaction>
  <Poll>F</Poll>
 </Session>
</WV-CSP-Message>
*/
static const char statusprim_wbxml[] = {
  0x03, 0x12, 0x6a, 0x00, 0xc9, 0x0b, 0x03, 0x31, 0x2e, 0x33, 0x00, 0x01,
  0x6d, 0x6e, 0x70, 0x80, 0x11, 0x01, 0x6f, 0x03, 0x69, 0x6d, 0x2e, 0x75,
  0x73, 0x65, 0x72, 0x2e, 0x63, 0x6f, 0x6d, 0x23, 0x34, 0x38, 0x38, 0x31,
  0x35, 0x40, 0x73, 0x65, 0x72, 0x76, 0x65, 0x72, 0x2e, 0x63, 0x6f, 0x6d,
  0x00, 0x01, 0x01, 0x72, 0x74, 0x76, 0x80, 0x21, 0x01, 0x75, 0x03, 0x49,
  0x4d, 0x41, 0x70, 0x70, 0x30, 0x31, 0x23, 0x31, 0x32, 0x33, 0x34, 0x35,
  0x40, 0x4e, 0x4f, 0x4b, 0x35, 0x31, 0x31, 0x30, 0x00, 0x01, 0x01, 0xf3,
  0x0d, 0x03, 0x31, 0x2e, 0x33, 0x00, 0x01, 0x71, 0x6a, 0x4b, 0xc3, 0x01,
  0xc9, 0x01, 0x52, 0x03, 0x50, 0x61, 0x72, 0x74, 0x69, 0x61, 0x6c, 0x6c,
  0x79, 0x20, 0x73, 0x75, 0x63, 0x63, 0x65, 0x73, 0x73, 0x66, 0x75, 0x6c,
  0x2e, 0x00, 0x01, 0x53, 0x4b, 0xc3, 0x02, 0x02, 0x13, 0x01, 0x52, 0x03,
  0x55, 0x6e, 0x6b, 0x6e, 0x6f, 0x77, 0x6e, 0x20, 0x75, 0x73, 0x65, 0x72,
  0x2e, 0x00, 0x01, 0x7a, 0x03, 0x77, 0x76, 0x3a, 0x62, 0x61, 0x64, 0x5f,
  0x75, 0x73, 0x65, 0x72, 0x31, 0x40, 0x69, 0x6d, 0x2e, 0x63, 0x6f, 0x6d,
  0x00, 0x01, 0x7a, 0x03, 0x77, 0x76, 0x3a, 0x62, 0x61, 0x64, 0x5f, 0x75,
  0x73, 0x65, 0x72, 0x32, 0x40, 0x69, 0x6d, 0x2e, 0x63, 0x6f, 0x6d, 0x00,
  0x01, 0x01, 0x53, 0x4b, 0xc3, 0x02, 0x02, 0x14, 0x01, 0x52, 0x03, 0x42,
  0x6c, 0x6f, 0x63, 0x6b, 0x65, 0x64, 0x2e, 0x00, 0x01, 0x7a, 0x03, 0x77,
  0x76, 0x3a, 0x62, 0x61, 0x64, 0x5f, 0x75, 0x73, 0x65, 0x72, 0x33, 0x40,
  0x69, 0x6d, 0x2e, 0x63, 0x6f, 0x6d, 0x00, 0x01, 0x7a, 0x03, 0x77, 0x76,
  0x3a, 0x62, 0x61, 0x64, 0x5f, 0x75, 0x73, 0x65, 0x72, 0x34, 0x40, 0x69,
  0x6d, 0x2e, 0x63, 0x6f, 0x6d, 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
  0x61, 0x80, 0x0b, 0x01, 0x01, 0x01,
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.openmobilealliance.org/DTD/WV-CSP1.2">
    <Session>
        <SessionDescriptor>
            <SessionType>Inband</SessionType>
            <SessionID>AA0BD762.00000000.test1</SessionID>
        </SessionDescriptor>
        <Transaction>
            <TransactionDescriptor>
                <TransactionMode>Request</TransactionMode>
                <TransactionID>transId8</TransactionID>
            </TransactionDescriptor>
            <TransactionContent xmlns="http://www.openmobilealliance.org/DTD/WV-TRC1.2">
                <SendMessage-Request>
                    <DeliveryReport>T</DeliveryReport>
                    <MessageInfo>
                        <Recipient>
                            <User>
                                <UserID>wv:test2</UserID>
                            </User>
                        </Recipient>
                        <Sender>
                            <User>
                                <UserID>wv:test1</UserID>
                            </User>
                        </Sender>
                        <DateTime>20070625T055652Z</DateTime>
                        <ContentSize>5</ContentSize>
                    </MessageInfo>
                    <ContentData>Today &#160;Today</ContentData>
                </SendMessage-Request>
            </TransactionContent>
        </Transaction>
    </Session>
</WV-CSP-Message>
*/
static const char sendmsgreq1_wbxml[] = {
  0x03, 0x10, 0x6a, 0x00, 0xc9, 0x08, 0x03, 0x31, 0x2e, 0x32, 0x00, 0x01,
  0x6d, 0x6e, 0x70, 0x80, 0x11, 0x01, 0x6f, 0x03, 0x41, 0x41, 0x30, 0x42,
  0x44, 0x37, 0x36, 0x32, 0x2e, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
  0x30, 0x2e, 0x74, 0x65, 0x73, 0x74, 0x31, 0x00, 0x01, 0x01, 0x72, 0x74,
  0x76, 0x80, 0x20, 0x01, 0x75, 0x03, 0x74, 0x72, 0x61, 0x6e, 0x73, 0x49,
  0x64, 0x38, 0x00, 0x01, 0x01, 0xf3, 0x0a, 0x03, 0x31, 0x2e, 0x32, 0x00,
  0x01, 0x00, 0x06, 0x57, 0x48, 0x80, 0x2c, 0x01, 0x53, 0x00, 0x00, 0x67,
  0x79, 0x7a, 0x03, 0x77, 0x76, 0x3a, 0x74, 0x65, 0x73, 0x74, 0x32, 0x00,
  0x01, 0x01, 0x01, 0x6c, 0x79, 0x7a, 0x03, 0x77, 0x76, 0x3a, 0x74, 0x65,
  0x73, 0x74, 0x31, 0x00, 0x01, 0x01, 0x01, 0x51, 0x03, 0x32, 0x30, 0x30,
  0x37, 0x30, 0x36, 0x32, 0x35, 0x54, 0x30, 0x35, 0x35, 0x36, 0x35, 0x32,
  0x5a, 0x00, 0x01, 0x4f, 0xc3, 0x01, 0x05, 0x01, 0x01, 0x4d, 0x03, 0x54,
  0x6f, 0x64, 0x61, 0x79, 0x20, 0x00, 0x02, 0x81, 0x20, 0x03, 0x54, 0x6f,
  0x64, 0x61, 0x79, 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.openmobilealliance.org/DTD/WV-CSP1.2">
    <Session>
        <SessionDescriptor>
            <SessionType>Inband</SessionType>
            <SessionID>196FE717.00000000.test1</SessionID>
        </SessionDescriptor>
        <Transaction>
            <TransactionDescriptor>
                <TransactionMode>Request</TransactionMode>
                <TransactionID>transId2</TransactionID>
            </TransactionDescriptor>
            <TransactionContent xmlns="http://www.openmobilealliance.org/DTD/WV-TRC1.2">
                <ClientCapability-Request>
                    <CapabilityList>
                        <ClientType>MOBILE_PHONE</ClientType>
                        <ParserSize>32767</ParserSize>
                        <MultiTrans>1</MultiTrans>
                        <InitialDeliveryMethod>P</InitialDeliveryMethod>
                        <ServerPollMin>2</ServerPollMin>
                        <SupportedBearer>HTTP</SupportedBearer>
                        <SupportedCIRMethod>STCP</SupportedCIRMethod>
                        <SupportedCIRMethod>SHTTP</SupportedCIRMethod>
                        <SupportedCIRMethod>SSMS</SupportedCIRMethod>
                    </CapabilityList>
                </ClientCapability-Request>
            </TransactionContent>
        </Transaction>
    </Session>
</WV-CSP-Message>
*/
static const char cap_request_wbxml[] = {
    0x03, 0x11, 0x6a, 0x00, 0xc9, 0x08, 0x03, 0x31, 0x2e, 0x32, 0x00, 0x01,
    0x6d, 0x6e, 0x70, 0x80, 0x11, 0x01, 0x6f, 0x03, 0x31, 0x39, 0x36, 0x46,
    0x45, 0x37, 0x31, 0x37, 0x2e, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
    0x30, 0x2e, 0x74, 0x65, 0x73, 0x74, 0x31, 0x00, 0x01, 0x01, 0x72, 0x74,
    0x76, 0x80, 0x20, 0x01, 0x75, 0x03, 0x74, 0x72, 0x61, 0x6e, 0x73, 0x49,
    0x64, 0x32, 0x00, 0x01, 0x01, 0xf3, 0x0a, 0x03, 0x31, 0x2e, 0x32, 0x00,
    0x01, 0x00, 0x01, 0x4c, 0x4a, 0x00, 0x05, 0x4f, 0x80, 0x6f, 0x01, 0x00,
    0x03, 0x4d, 0xc3, 0x02, 0x7f, 0xff, 0x01, 0x4c, 0xc3, 0x01, 0x01, 0x01,
    0x4b, 0x80, 0x1f, 0x01, 0x4e, 0xc3, 0x01, 0x02, 0x01, 0x4f, 0x80, 0x42,
    0x01, 0x50, 0x80, 0x44, 0x01, 0x50, 0x80, 0x81, 0x25, 0x01, 0x50, 0x80,
    0x81, 0x24, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01,
};

/*
<?xml version="1.0" encoding="UTF-8" ?>
<WV-CSP-Message xmlns="http://www.wireless-village.org/CSP1.1">
    <sometag>
        <Session>
            <someMoreTag/>
        </Session>
    </sometag>
    <metag attr="sometag"/>
    <tag movie="transformer"></tag>
</WV-CSP-Message>
*/
static const char literal_wbxml[] = {
  0x03, 0x10, 0x6a, 0x29, 0x73, 0x6f, 0x6d, 0x65, 0x74, 0x61, 0x67, 0x00,
  0x73, 0x6f, 0x6d, 0x65, 0x4d, 0x6f, 0x72, 0x65, 0x54, 0x61, 0x67, 0x00,
  0x6d, 0x65, 0x74, 0x61, 0x67, 0x00, 0x61, 0x74, 0x74, 0x72, 0x00, 0x74,
  0x61, 0x67, 0x00, 0x6d, 0x6f, 0x76, 0x69, 0x65, 0x00, 0xc9, 0x05, 0x03,
  0x31, 0x2e, 0x31, 0x00, 0x01, 0x44, 0x00, 0x6d, 0x04, 0x08, 0x01, 0x01,
  0x84, 0x14, 0x04, 0x1a, 0x83, 0x00, 0x01, 0x84, 0x1f, 0x04, 0x23, 0x03,
  0x74, 0x72, 0x61, 0x6e, 0x73, 0x66, 0x6f, 0x72, 0x6d, 0x65, 0x72, 0x00,
  0x01, 0x01
};

class SimpleTestHandler: public DefaultWbxmlContentHandler
{
public:
    void startElement(const char *name, const vector<Attribute> & attribs)
    {
        startElemName = name;
        this->attribs = attribs;
    }
    void endElement(const char *name)
    {
        endElemName = name;
    }
    string startElemName;
    string endElemName;
    vector<Attribute> attribs;
};

struct DomElement
{
    DomElement()
    {
        intVal = 0;
    }

    string name;
    vector<Attribute> attribs;
    vector<DomElement> children;
    string characters;
    uint32_t intVal;
};

class DomTestHandler: public DefaultWbxmlContentHandler
{
public:
    DomTestHandler()
    {
        currentElement = NULL;
    }

    void startElement(const char *name, const vector<Attribute> & attribs)
    {
        if (!currentElement) {
            currentElement = &root;
            currentElement->name = name;
            currentElement->attribs = attribs;
        } else {
            DomElement child;
            child.name = name;
            child.attribs = attribs;
            currentElement->children.push_back(child);
            elemStack.push_back(currentElement);
            currentElement = &(currentElement->children.back());
        }
    }

    void endElement(const char *name)
    {
        TEST_ASSERT_EQUAL_STRING(name, currentElement->name.c_str());
        TEST_ASSERT((currentElement == &root) ? !elemStack.size() : elemStack.size() > 0);
        if (elemStack.size()) {
            currentElement = elemStack.back();
            elemStack.pop_back();
        }
    }

    void characters(const char * data, int len)
    {
        string s(data, len);
        currentElement->characters.append(data, len);
    }

    void opaque(const char * data, int len)
    {
        if (currentElement->name == "Code") {
            while (len--) {
                currentElement->intVal <<= 8;
                currentElement->intVal |= (unsigned char)*data;
                data++;
            }
        }
    }

    DomElement root;

private:
    vector<DomElement *> elemStack;
    DomElement *currentElement;
};

static void setUp(void)
{
}

static void tearDown(void)
{
}

static void testSimpleCspRet(void)
{
    WbxmlParser parser(0);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(simple_wbxml,
                sizeof(simple_wbxml), true));
}

static void testSimpleCspPublicId(void)
{
    WbxmlParser parser(0);
    DefaultWbxmlContentHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(simple_wbxml,
                sizeof(simple_wbxml), true));
    TEST_ASSERT_EQUAL_INT(0x10 /* WV CSP 1.1 */, handler.getPublicId());
}

static void testSimpleCspStartElement(void)
{
    WbxmlParser parser(0);
    SimpleTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(simple_wbxml,
                sizeof(simple_wbxml), true));
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", handler.startElemName.c_str());
}

static void testSimpleCspStartElementAttr(void)
{
    WbxmlParser parser(0);
    SimpleTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(simple_wbxml,
                sizeof(simple_wbxml), true));
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", handler.startElemName.c_str());
    TEST_ASSERT_EQUAL_INT(1, handler.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", handler.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.wireless-village.org/CSP1.1", handler.attribs[0].value.c_str());
}

static void testSimpleCspEndElement(void)
{
    WbxmlParser parser(0);
    SimpleTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(simple_wbxml,
                sizeof(simple_wbxml), true));
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", handler.endElemName.c_str());
}

static void testSimpleCspNoContentTag(void)
{
    WbxmlParser parser(0);
    SimpleTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(simple_nocontent_wbxml,
                sizeof(simple_nocontent_wbxml), true));
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", handler.startElemName.c_str());
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", handler.endElemName.c_str());
    TEST_ASSERT_EQUAL_INT(1, handler.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", handler.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.wireless-village.org/CSP1.1", handler.attribs[0].value.c_str());
}

static void testSimpleCspLevel2(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(simple2_wbxml,
                sizeof(simple2_wbxml), true));
    const DomElement &root = handler.root;
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", root.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, root.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", root.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.wireless-village.org/CSP1.1", root.attribs[0].value.c_str());

    TEST_ASSERT_EQUAL_INT(1, root.children.size());
    const DomElement &elem = root.children[0];
    TEST_ASSERT_EQUAL_INT(0, elem.children.size());
    TEST_ASSERT_EQUAL_STRING("Session", elem.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, elem.attribs.size());
}

static void testSimpleCspLevel3Characters(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(simple3_wbxml,
                sizeof(simple3_wbxml), true));

    const DomElement &root = handler.root;
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", root.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, root.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", root.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.wireless-village.org/CSP1.1", root.attribs[0].value.c_str());

    TEST_ASSERT_EQUAL_INT(1, root.children.size());
    const DomElement &session = root.children[0];
    TEST_ASSERT_EQUAL_INT(1, session.children.size());
    TEST_ASSERT_EQUAL_STRING("Session", session.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, session.attribs.size());

    const DomElement &sessionDesc = session.children[0];
    TEST_ASSERT_EQUAL_INT(1, sessionDesc.children.size());
    TEST_ASSERT_EQUAL_STRING("SessionDescriptor", sessionDesc.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, sessionDesc.attribs.size());

    const DomElement &sessionType = sessionDesc.children[0];
    TEST_ASSERT_EQUAL_INT(0, sessionType.children.size());
    TEST_ASSERT_EQUAL_STRING("SessionType", sessionType.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, sessionType.attribs.size());
    TEST_ASSERT_EQUAL_STRING("Outband", sessionType.characters.c_str());
}

/* String table, LITERAL* for tag, LITERAL for attribute, STR_T */
static void testLiteralAndStrTable(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(literal_wbxml,
                sizeof(literal_wbxml), true));
    const DomElement &sometag = handler.root.children[0];
    TEST_ASSERT_EQUAL_STRING("sometag", sometag.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, sometag.attribs.size());
    TEST_ASSERT_EQUAL_INT(1, sometag.children.size());

    const DomElement &someMoreTag = sometag.children[0].children[0];
    TEST_ASSERT_EQUAL_STRING("someMoreTag", someMoreTag.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, someMoreTag.attribs.size());
    TEST_ASSERT_EQUAL_INT(0, someMoreTag.children.size());

    const DomElement &metag = handler.root.children[1];
    TEST_ASSERT_EQUAL_STRING("metag", metag.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, metag.attribs.size());
    TEST_ASSERT_EQUAL_STRING("attr", metag.attribs[0].name);
    TEST_ASSERT_EQUAL_STRING("sometag", metag.attribs[0].value);
    TEST_ASSERT_EQUAL_INT(0, metag.children.size());

    const DomElement &tag = handler.root.children[2];
    TEST_ASSERT_EQUAL_STRING("tag", tag.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, tag.attribs.size());
    TEST_ASSERT_EQUAL_STRING("movie", tag.attribs[0].name);
    TEST_ASSERT_EQUAL_STRING("transformer", tag.attribs[0].value);
    TEST_ASSERT_EQUAL_INT(0, tag.children.size());
}

/* SWITCH_PAGE */
static void testCspLoginResponse(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(loginresponse_wbxml,
                sizeof(loginresponse_wbxml), true));

    const DomElement &root = handler.root;
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", root.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, root.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", root.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.wireless-village.org/CSP1.1",
            root.attribs[0].value.c_str());

    const DomElement &transaction = root.children[0].children[1];
    TEST_ASSERT_EQUAL_STRING("Transaction", transaction.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, transaction.attribs.size());
    TEST_ASSERT_EQUAL_INT(2, transaction.children.size());

    const DomElement &transactionContent = transaction.children[1];
    TEST_ASSERT_EQUAL_STRING("TransactionContent", transactionContent.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, transactionContent.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", transactionContent.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.wireless-village.org/TRC1.1",
            transactionContent.attribs[0].value.c_str());
    TEST_ASSERT_EQUAL_INT(1, transactionContent.children.size());

    const DomElement &clientID = transactionContent.children[0].children[0];
    TEST_ASSERT_EQUAL_STRING("ClientID", clientID.name.c_str());
    TEST_ASSERT_EQUAL_STRING("Esmertec112233", clientID.characters.c_str());
    TEST_ASSERT_EQUAL_INT(0, clientID.attribs.size());
    TEST_ASSERT_EQUAL_INT(0, clientID.children.size());

    const DomElement &capReq = transactionContent.children[0].children[4];
    TEST_ASSERT_EQUAL_STRING("CapabilityRequest", capReq.name.c_str());
    TEST_ASSERT_EQUAL_STRING("F", capReq.characters.c_str());
    TEST_ASSERT_EQUAL_INT(0, capReq.attribs.size());
    TEST_ASSERT_EQUAL_INT(0, capReq.children.size());
}

/* OPAQUE integer */
static void testCspStatusPrim(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(statusprim_wbxml,
                sizeof(statusprim_wbxml), true));

    const DomElement &root = handler.root;
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", root.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, root.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", root.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.openmobilealliance.org/DTD/IMPS-CSP1.3",
            root.attribs[0].value.c_str());

    const DomElement &transacContent = root.children[0].children[1].children[1];
    TEST_ASSERT_EQUAL_STRING("TransactionContent", transacContent.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, transacContent.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", transacContent.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.openmobilealliance.org/DTD/IMPS-TRC1.3",
            transacContent.attribs[0].value.c_str());

    const DomElement &resultCode = transacContent.children[0].children[0].children[0];
    TEST_ASSERT_EQUAL_STRING("Code", resultCode.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, resultCode.attribs.size());
    TEST_ASSERT_EQUAL_INT(201, resultCode.intVal);

    const DomElement &detailedResultCode = transacContent.children[0].children[0].children[2].children[0];
    TEST_ASSERT_EQUAL_STRING("Code", detailedResultCode.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, detailedResultCode.attribs.size());
    TEST_ASSERT_EQUAL_INT(531, detailedResultCode.intVal);
}

/* SWITCH_PAGE, OPAQUE integer, ENTITY */
static void testCspSendMsg(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml,
                sizeof(sendmsgreq1_wbxml), true));

    const DomElement &root = handler.root;
    const DomElement &transacContent = root.children[0].children[1].children[1];
    const DomElement &contentData = transacContent.children[0].children[2];

    TEST_ASSERT_EQUAL_STRING("ContentData", contentData.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, contentData.attribs.size());
    // &#160; => U+00A0, UTF-8 0xC2 0xA0
    TEST_ASSERT_EQUAL_STRING("Today \xc2\xa0Today", contentData.characters.c_str());
}

/* Token values, EXT_0 + mbuint32 */
static void testCspCapRequest(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(cap_request_wbxml,
                sizeof(cap_request_wbxml), true));

    const DomElement &root = handler.root;
    const DomElement &transacContent = root.children[0].children[1].children[1];
    const DomElement &capList = transacContent.children[0].children[0];

    TEST_ASSERT_EQUAL_STRING("CapabilityList", capList.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, capList.attribs.size());
    TEST_ASSERT_EQUAL_INT(9, capList.children.size());

    TEST_ASSERT_EQUAL_STRING("ClientType", capList.children[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("MOBILE_PHONE", capList.children[0].characters.c_str());

    TEST_ASSERT_EQUAL_STRING("SupportedBearer", capList.children[5].name.c_str());
    TEST_ASSERT_EQUAL_STRING("HTTP", capList.children[5].characters.c_str());

    TEST_ASSERT_EQUAL_STRING("SupportedCIRMethod", capList.children[7].name.c_str());
    TEST_ASSERT_EQUAL_STRING("SHTTP", capList.children[7].characters.c_str());

    TEST_ASSERT_EQUAL_STRING("SupportedCIRMethod", capList.children[8].name.c_str());
    TEST_ASSERT_EQUAL_STRING("SSMS", capList.children[8].characters.c_str());
}

static void testIncompleteInlineString(void)
{
    WbxmlParser parser(0);
    ASSERT_EQUAL_INT(WBXML_STATUS_ERROR, parser.parse(incomplete_str_i_wbxml,
                sizeof(incomplete_str_i_wbxml), true));
}

/* more ENTITY tests */
static void testBigEntity(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(big_entity_wbxml,
                sizeof(big_entity_wbxml), true));

    const DomElement &sessionType = handler.root.children[0].children[0].children[0];
    TEST_ASSERT_EQUAL_INT(0, sessionType.children.size());
    TEST_ASSERT_EQUAL_STRING("SessionType", sessionType.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, sessionType.attribs.size());
    TEST_ASSERT_EQUAL_STRING("\xf4\x8f\xbf\xbf", sessionType.characters.c_str());
}

static void testInvalidEntity(void)
{
    WbxmlParser parser(0);
    ASSERT_EQUAL_INT(WBXML_STATUS_ERROR, parser.parse(invalid_entity_wbxml,
                sizeof(invalid_entity_wbxml), true));
}

static void testParseInChunk(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    // wbxml version
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml,
                1, false));

    // public id + charset
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml + 1,
                2, false));

    // 0 byte long string table
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml + 3,
                1, false));

    // first start tag
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml + 4,
                1, false));

    // some data
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml + 5,
                5, false));

    // some more data
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml + 10,
                sizeof(sendmsgreq1_wbxml) - 1 - 2 - 1 - 1 - 5 - 4, false));

    // last 4 bytes
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml
                + sizeof(sendmsgreq1_wbxml) - 4, 4, true));

    const DomElement &root = handler.root;
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", root.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, root.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", root.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.openmobilealliance.org/DTD/WV-CSP1.2",
            root.attribs[0].value.c_str());
}

static void testParserReset(void)
{
    WbxmlParser parser(0);

    // first some incomplete data
    ASSERT_EQUAL_INT(WBXML_STATUS_ERROR, parser.parse(sendmsgreq1_wbxml,
                1, true));

    parser.reset();

    // now some correct data: wbxml version
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml,
                1, false));

    // more correct data: public id + charset
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(sendmsgreq1_wbxml + 1,
                2, false));

    parser.reset();

    // some wrong data again
    ASSERT_EQUAL_INT(WBXML_STATUS_ERROR, parser.parse(sendmsgreq1_wbxml + 3,
                6, false));

    parser.reset();

    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(big_entity_wbxml,
                sizeof(big_entity_wbxml), true));

    const DomElement &sessionType = handler.root.children[0].children[0].children[0];
    TEST_ASSERT_EQUAL_INT(0, sessionType.children.size());
    TEST_ASSERT_EQUAL_STRING("SessionType", sessionType.name.c_str());

    parser.reset();
    handler.root.name.clear();
    handler.root.children.clear();

    // parse same document without setting a handler
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(big_entity_wbxml,
                sizeof(big_entity_wbxml), true));
    TEST_ASSERT_EQUAL_INT(0, handler.root.children.size());
    TEST_ASSERT_EQUAL_INT(0, handler.root.name.size());
}

static void testIncompleteData(void)
{
    WbxmlParser parser(0);
    ASSERT_EQUAL_INT(WBXML_STATUS_ERROR, parser.parse(sendmsgreq1_wbxml,
                1, true));

    WbxmlParser parser1(0);
    ASSERT_EQUAL_INT(WBXML_STATUS_ERROR, parser1.parse(sendmsgreq1_wbxml,
                3, true));

    WbxmlParser parser2(0);
    ASSERT_EQUAL_INT(WBXML_STATUS_ERROR, parser2.parse(sendmsgreq1_wbxml,
                4, true));

    WbxmlParser parser3(0);
    ASSERT_EQUAL_INT(WBXML_STATUS_ERROR, parser3.parse(sendmsgreq1_wbxml,
                5, true));
}

extern "C" TestRef ImpsParserTest_tests(void)
{
    EMB_UNIT_TESTFIXTURES(fixtures) {
        DEFINE_TEST(testSimpleCspRet),
        DEFINE_TEST(testSimpleCspPublicId),
        DEFINE_TEST(testSimpleCspStartElement),
        DEFINE_TEST(testSimpleCspStartElementAttr),
        DEFINE_TEST(testSimpleCspEndElement),
        DEFINE_TEST(testSimpleCspNoContentTag),
        DEFINE_TEST(testSimpleCspLevel2),
        DEFINE_TEST(testSimpleCspLevel3Characters),
        DEFINE_TEST(testLiteralAndStrTable),
        DEFINE_TEST(testCspLoginResponse),
        DEFINE_TEST(testCspStatusPrim),
        DEFINE_TEST(testCspSendMsg),
        DEFINE_TEST(testCspCapRequest),
        DEFINE_TEST(testIncompleteInlineString),
        DEFINE_TEST(testBigEntity),
        DEFINE_TEST(testInvalidEntity),
        DEFINE_TEST(testParseInChunk),
        DEFINE_TEST(testParserReset),
        DEFINE_TEST(testIncompleteData),
    };
    EMB_UNIT_TESTCALLER(ImpsParserTest, "ImpsParserTest", setUp, tearDown, fixtures);

    // temporary work around for the linker/loader problem of the sooner build
    static TestCaller t = ImpsParserTest;
    t.isa = (TestImplement *)&TestCallerImplement;
    return (TestRef)&t;
}

