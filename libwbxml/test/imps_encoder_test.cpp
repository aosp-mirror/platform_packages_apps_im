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
#include <string.h>
#include <stdlib.h>
#include "xml2wbxml.h"
#include "imps_encoder.h"

#define DEFINE_TEST(test)   \
    new_TestFixture(#test, test)

#define ASSERT_EQUAL_INT(expected, actual) \
{   \
    int tmp = actual;   /* avoid duplicated evaluation in TEST_ASSERT_EQUAL_INT */  \
    TEST_ASSERT_EQUAL_INT(expected, tmp);   \
}

#define ASSERT_EQUAL_BINARY_CPPSTRING(expected, cpp_string)   \
{   \
    ASSERT_EQUAL_INT(sizeof(expected), cpp_string.size()); \
    ASSERT_EQUAL_INT(0, memcmp(expected, cpp_string.c_str(), sizeof(expected)));    \
}

#define ASSERT_EQUAL_CSTRING_CPPSTRING(expected, cpp_string)   \
{   \
    ASSERT_EQUAL_INT(sizeof(expected) - 1, cpp_string.size()); \
    ASSERT_EQUAL_INT(0, memcmp(expected, cpp_string.c_str(), sizeof(expected) - 1));    \
}

static const char simple_csp12_xml[] = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    "<WV-CSP-Message xmlns=\"http://www.openmobilealliance.org/DTD/WV-CSP1.2\"></WV-CSP-Message>"
};

static const char simple_csp12_doctype_xml[] = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    "<!DOCTYPE WV-CSP PUBLIC \"-//OMA//DTD WV-CSP 1.2//EN\" "
    "  \"http://www.openmobilealliance.org/DTD/WV-CSP.DTD\">"
    "<WV-CSP-Message xmlns=\"http://www.openmobilealliance.org/DTD/WV-CSP1.2\"></WV-CSP-Message>"
};

static const char simple_csp12_wbxml[] = {
    0x03, 0x11, 0x6a, 0x00, 0xc9, 0x08, 0x03, 0x31, 0x2e, 0x32, 0x00, 0x01, 0x01
};

static const char simple2_csp11_xml[] = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    "<WV-CSP-Message xmlns=\"http://www.wireless-village.org/CSP1.1\">"
    "<Session></Session>"
    "</WV-CSP-Message>"
};

static const char simple2_csp11_wbxml[] = {
    0x03, 0x10, 0x6a, 0x00, 0xc9, 0x05, 0x03, 0x31, 0x2e, 0x31, 0x00, 0x01,
    0x6d, 0x01, 0x01
};

static const char simple_content_xml[] = {
"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
"<WV-CSP-Message xmlns=\"http://www.openmobilealliance.org/DTD/WV-CSP1.2\">"
"    <Session>"
"        <SessionDescriptor>"
"            <SessionType>Outband</SessionType>"
"        </SessionDescriptor>"
"    </Session>"
"</WV-CSP-Message>"
};

static const char simple_content_wbxml[] = {
    0x03, 0x11, 0x6a, 0x00, 0xc9, 0x08, 0x03, 0x31, 0x2e, 0x32, 0x00, 0x01,
    0x6d, 0x6e, 0x70, 0x80, 0x19, 0x01, 0x01, 0x01, 0x01
};

// libwbxml2 generates 0xC3 0x00 for integer value 0, but we take the safer
// way 0xC3 0x01 0x00
static const char simple_integer_xml[] = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    "<WV-CSP-Message xmlns=\"http://www.openmobilealliance.org/DTD/WV-CSP1.2\">"
    "<Session>"
    "    <ClientID>9527</ClientID>"
    "    <Result>"
    "        <Code>401</Code>"
    "        <Description>401</Description>"
    "    </Result>"
    "    <ContentSize>4294967295</ContentSize>"
    "    <MessageCount>0</MessageCount>"
    "</Session>"
    "</WV-CSP-Message>"
};
static const char simple_integer_wbxml[] = {
    0x03, 0x11, 0x6a, 0x00, 0xc9, 0x08, 0x03, 0x31, 0x2e, 0x32, 0x00, 0x01,
    0x6d, 0x4a, 0x03, 0x39, 0x35, 0x32, 0x37, 0x00, 0x01, 0x6a, 0x4b, 0xc3,
    0x02, 0x01, 0x91, 0x01, 0x52, 0x03, 0x34, 0x30, 0x31, 0x00, 0x01, 0x01,
    0x4f, 0xc3, 0x04, 0xff, 0xff, 0xff, 0xff, 0x01, 0x5a, 0xc3, 0x01, 0x00,
    0x01, 0x01, 0x01
};

static const char simple_datetime_xml[] = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    "<WV-CSP-Message xmlns=\"http://www.openmobilealliance.org/DTD/WV-CSP1.2\">"
    "<Session>"
    "    <DateTime>20010925T165859Z</DateTime>"
    "    <DeliveryTime>20010925T165859Z</DeliveryTime>"
    "</Session>"
    "</WV-CSP-Message>"
};
static const char simple_datetime_wbxml[] = {
    0x03, 0x11, 0x6a, 0x00, 0xc9, 0x08, 0x03, 0x31, 0x2e, 0x32, 0x00, 0x01,
    0x6d, 0x51, 0xc3, 0x06, 0x1f, 0x46, 0x73, 0x0e, 0xbb, 0x5a, 0x01, 0x00,
    0x06, 0x5a, 0xc3, 0x06, 0x1f, 0x46, 0x73, 0x0e, 0xbb, 0x5a, 0x01, 0x01,
    0x01
};

static const char simple_switch_page_xml[] = {
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    "<WV-CSP-Message xmlns=\"http://www.openmobilealliance.org/DTD/WV-CSP1.2\">"
    "<Session>"
    "    <Login-Response>"
    "        <ClientID>Esmertec112233</ClientID>"
    "        <Result>"
    "            <Description>blahblah</Description>"
    "        </Result>"
    "    </Login-Response>"
    "</Session>"
    "</WV-CSP-Message>"
};
static const char simple_switch_page_wbxml[] = {
    0x03, 0x11, 0x6a, 0x00, 0xc9, 0x08, 0x03, 0x31, 0x2e, 0x32, 0x00, 0x01,
    0x6d, 0x00, 0x01, 0x5e, 0x00, 0x00, 0x4a, 0x03, 0x45, 0x73, 0x6d, 0x65,
    0x72, 0x74, 0x65, 0x63, 0x31, 0x31, 0x32, 0x32, 0x33, 0x33, 0x00, 0x01,
    0x6a, 0x52, 0x03, 0x62, 0x6c, 0x61, 0x68, 0x62, 0x6c, 0x61, 0x68, 0x00,
    0x01, 0x01, 0x01, 0x01, 0x01
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

static const char cap_request_xml[] = {
"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
"<WV-CSP-Message xmlns=\"http://www.openmobilealliance.org/DTD/WV-CSP1.2\">"
"    <Session>"
"        <SessionDescriptor>"
"            <SessionType>Inband</SessionType>"
"            <SessionID>196FE717.00000000.test1</SessionID>"
"        </SessionDescriptor>"
"        <Transaction>"
"            <TransactionDescriptor>"
"                <TransactionMode>Request</TransactionMode>"
"                <TransactionID>transId2</TransactionID>"
"            </TransactionDescriptor>"
"            <TransactionContent xmlns=\"http://www.openmobilealliance.org/DTD/WV-TRC1.2\">"
"                <ClientCapability-Request>"
"                    <CapabilityList>"
"                        <ClientType>MOBILE_PHONE</ClientType>"
"                        <ParserSize>32767</ParserSize>"
"                        <MultiTrans>1</MultiTrans>"
"                        <InitialDeliveryMethod>P</InitialDeliveryMethod>"
"                        <ServerPollMin>2</ServerPollMin>"
"                        <SupportedBearer>HTTP</SupportedBearer>"
"                        <SupportedCIRMethod>STCP</SupportedCIRMethod>"
"                        <SupportedCIRMethod>SHTTP</SupportedCIRMethod>"
"                        <SupportedCIRMethod>SSMS</SupportedCIRMethod>"
"                    </CapabilityList>"
"                </ClientCapability-Request>"
"            </TransactionContent>"
"        </Transaction>"
"    </Session>"
"</WV-CSP-Message>"
};
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

static const char statusprim_xml[] = {
"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
"<WV-CSP-Message xmlns=\"http://www.openmobilealliance.org/DTD/IMPS-CSP1.3\">"
" <Session>"
"  <SessionDescriptor>"
"   <SessionType>Inband</SessionType>"
"   <SessionID>im.user.com#48815@server.com</SessionID>"
"  </SessionDescriptor>"
"  <Transaction>"
"   <TransactionDescriptor>"
"    <TransactionMode>Response</TransactionMode>"
"    <TransactionID>IMApp01#12345@NOK5110</TransactionID>"
"   </TransactionDescriptor>"
"   <TransactionContent xmlns=\"http://www.openmobilealliance.org/DTD/IMPS-TRC1.3\">"
"    <Status>"
"     <Result>"
"      <Code>201</Code>"
"      <Description>Partially successful.</Description>"
"      <DetailedResult>"
"       <Code>531</Code>"
"       <Description>Unknown user.</Description>"
"       <UserID>wv:bad_user1@im.com</UserID>"
"       <UserID>wv:bad_user2@im.com</UserID>"
"      </DetailedResult>"
"      <DetailedResult>"
"       <Code>532</Code>"
"       <Description>Blocked.</Description>"
"       <UserID>wv:bad_user3@im.com</UserID>"
"       <UserID>wv:bad_user4@im.com</UserID>"
"      </DetailedResult>"
"     </Result>"
"    </Status>"
"   </TransactionContent>"
"  </Transaction>"
"  <Poll>F</Poll>"
" </Session>"
"</WV-CSP-Message>"
};

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
  0x61, 0x80, 0x0b, 0x01, 0x01, 0x01
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

class DataHandler : public WbxmlHandler
{
public:
    void wbxmlData(const char *data, uint32_t len)
    {
        wbxml.append(data, len);
#if 0
        for (uint32_t i = 0; i < len; i++) {
            if (!(i % 12))
                printf ("\n");
            printf ("0x%02x, ", (unsigned char)data[i]);
        }
        printf ("\n");
#endif
    }
    string wbxml;
};

static void setUp(void)
{
}

static void tearDown(void)
{
}

static void testSmokeTest(void)
{
    Xml2WbxmlEncoder encoder;
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(simple_csp12_xml,
                strlen(simple_csp12_xml), true));

    Xml2WbxmlEncoder encoder1;
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder1.encode(simple_csp12_doctype_xml,
                strlen(simple_csp12_doctype_xml), true));
}

static void testSimpleCsp12(void)
{
    Xml2WbxmlEncoder encoder;
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(simple_csp12_doctype_xml,
                strlen(simple_csp12_doctype_xml), true));
    ASSERT_EQUAL_BINARY_CPPSTRING(simple_csp12_wbxml, handler.wbxml);

    Xml2WbxmlEncoder encoder1;
    DataHandler handler1;
    encoder1.setWbxmlHandler(&handler1);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder1.encode(simple_csp12_xml,
                strlen(simple_csp12_xml), true));
    ASSERT_EQUAL_BINARY_CPPSTRING(simple_csp12_wbxml, handler.wbxml);
}

static void testSimpleCsp11Level2(void)
{
    Xml2WbxmlEncoder encoder;
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(simple2_csp11_xml,
                strlen(simple2_csp11_xml), true));
    ASSERT_EQUAL_BINARY_CPPSTRING(simple2_csp11_wbxml, handler.wbxml);
}

static void testSimpleContent(void)
{
    Xml2WbxmlEncoder encoder;
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(simple_content_xml,
                strlen(simple_content_xml), true));
    ASSERT_EQUAL_BINARY_CPPSTRING(simple_content_wbxml, handler.wbxml);
}

static void testSimpleOpaqueInteger(void)
{
    Xml2WbxmlEncoder encoder;
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(simple_integer_xml,
                strlen(simple_integer_xml), true));
    ASSERT_EQUAL_BINARY_CPPSTRING(simple_integer_wbxml, handler.wbxml);
}

static void testSimpleOpaqueDatetime(void)
{
    Xml2WbxmlEncoder encoder;
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(simple_datetime_xml,
                strlen(simple_datetime_xml), true));
    ASSERT_EQUAL_BINARY_CPPSTRING(simple_datetime_wbxml, handler.wbxml);
}

static void testSimpleSwitchPage(void)
{
    Xml2WbxmlEncoder encoder;
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(simple_switch_page_xml,
                strlen(simple_switch_page_xml), true));
    ASSERT_EQUAL_BINARY_CPPSTRING(simple_switch_page_wbxml, handler.wbxml);
}

/* various token values, EXT_0 + mbuint32 */
static void testCapRequest(void)
{
    Xml2WbxmlEncoder encoder;
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(cap_request_xml,
                strlen(cap_request_xml), true));
    ASSERT_EQUAL_BINARY_CPPSTRING(cap_request_wbxml, handler.wbxml);
}

static void testEncodeInChunk(void)
{
    Xml2WbxmlEncoder encoder;
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);

    int chunkSize = strlen(statusprim_xml) / 4;

    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(statusprim_xml,
                chunkSize, false));

    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(statusprim_xml + chunkSize,
                chunkSize, false));

    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(statusprim_xml + chunkSize * 2,
                chunkSize, false));

    ASSERT_EQUAL_INT(WBXML_STATUS_OK, encoder.encode(statusprim_xml + chunkSize * 3,
                strlen(statusprim_xml) - chunkSize * 3, true));

    ASSERT_EQUAL_BINARY_CPPSTRING(statusprim_wbxml, handler.wbxml);
}

static void testImpsEncoderSmokeTest(void)
{
    ImpsWbxmlEncoder encoder(PUBLICID_IMPS_1_3);
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    const char * atts[] = {NULL};
    ASSERT_EQUAL_INT(NO_ERROR, encoder.startElement("WV-CSP-Message", atts));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.endElement());
    ASSERT_EQUAL_INT(6, handler.wbxml.size());
    ASSERT_EQUAL_CSTRING_CPPSTRING("\x03\x12\x6a\x00\x49\x01", handler.wbxml);
}

static void testImpsEncoderReset(void)
{
    ImpsWbxmlEncoder encoder(PUBLICID_IMPS_1_3);
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    const char * atts[] = {NULL};
    ASSERT_EQUAL_INT(NO_ERROR, encoder.startElement("WV-CSP-Message", atts));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.endElement());
    ASSERT_EQUAL_INT(6, handler.wbxml.size());
    ASSERT_EQUAL_CSTRING_CPPSTRING("\x03\x12\x6a\x00\x49\x01", handler.wbxml);

    encoder.reset();
    handler.wbxml.clear();

    ASSERT_EQUAL_INT(NO_ERROR, encoder.startElement("WV-CSP-Message", atts));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.startElement("Session", atts));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.endElement());
    ASSERT_EQUAL_INT(NO_ERROR, encoder.endElement());
    ASSERT_EQUAL_INT(8, handler.wbxml.size());
    ASSERT_EQUAL_CSTRING_CPPSTRING("\x03\x12\x6a\x00\x49\x6d\x01\x01", handler.wbxml);
}

static void testImpsStringContentData(void)
{
    ImpsWbxmlEncoder encoder(PUBLICID_IMPS_1_3);
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    const char * atts[] = {NULL};
    ASSERT_EQUAL_INT(NO_ERROR, encoder.startElement("ContentData", atts));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.characters("Hello", 5));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.endElement());
    ASSERT_EQUAL_INT(13, handler.wbxml.size());
    ASSERT_EQUAL_CSTRING_CPPSTRING("\x03\x12\x6a\x00\x4d\x03Hello\x00\x01", handler.wbxml);
}

static void testImpsOpaqueContentData(void)
{
    ImpsWbxmlEncoder encoder(PUBLICID_IMPS_1_3);
    DataHandler handler;
    encoder.setWbxmlHandler(&handler);
    const char * atts[] = {NULL};
    ASSERT_EQUAL_INT(NO_ERROR, encoder.startElement("ContentData", atts));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.opaque("Hello", 5));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.endElement());
    ASSERT_EQUAL_INT(13, handler.wbxml.size());
    ASSERT_EQUAL_CSTRING_CPPSTRING("\x03\x12\x6a\x00\x4d\xc3\x05Hello\x01", handler.wbxml);

    encoder.reset();
    handler.wbxml.clear();

    const int dataSize = 500000;    // 7a120, MB uint32 "\x9e\xc2\x20"
    char * data = new char[dataSize];
    memset(data, 7, dataSize);

    ASSERT_EQUAL_INT(NO_ERROR, encoder.startElement("ContentData", atts));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.opaque(data,dataSize));
    ASSERT_EQUAL_INT(NO_ERROR, encoder.endElement());

    ASSERT_EQUAL_INT(dataSize + 9 + 1, handler.wbxml.size());
    ASSERT_EQUAL_INT(0, memcmp("\x03\x12\x6a\x00\x4d\xc3\x9e\xc2\x20", handler.wbxml.c_str(), 9));
    ASSERT_EQUAL_INT(0, memcmp(data, handler.wbxml.c_str() + 9, dataSize));
    ASSERT_EQUAL_INT(0x01, handler.wbxml[9 + dataSize]);
    delete [] data;
}

#if 0

/* OPAQUE integer */
static void testCspStatusPrim(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(statusprim_wbxml,
                sizeof(statusprim_wbxml), true));

    Element &root = handler.root;
    TEST_ASSERT_EQUAL_STRING("WV-CSP-Message", root.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, root.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", root.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.openmobilealliance.org/DTD/IMPS-CSP1.3",
            root.attribs[0].value.c_str());

    Element &transacContent = root.children[0].children[1].children[1];
    TEST_ASSERT_EQUAL_STRING("TransactionContent", transacContent.name.c_str());
    TEST_ASSERT_EQUAL_INT(1, transacContent.attribs.size());
    TEST_ASSERT_EQUAL_STRING("xmlns", transacContent.attribs[0].name.c_str());
    TEST_ASSERT_EQUAL_STRING("http://www.openmobilealliance.org/DTD/IMPS-TRC1.3",
            transacContent.attribs[0].value.c_str());

    Element &resultCode = transacContent.children[0].children[0].children[0];
    TEST_ASSERT_EQUAL_STRING("Code", resultCode.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, resultCode.attribs.size());
    TEST_ASSERT_EQUAL_INT(201, resultCode.intVal);

    Element &detailedResultCode = transacContent.children[0].children[0].children[2].children[0];
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

    Element &root = handler.root;
    Element &transacContent = root.children[0].children[1].children[1];
    Element &contentData = transacContent.children[0].children[2];

    TEST_ASSERT_EQUAL_STRING("ContentData", contentData.name.c_str());
    TEST_ASSERT_EQUAL_INT(0, contentData.attribs.size());
    // &#160; => U+00A0, UTF-8 0xC2 0xA0
    TEST_ASSERT_EQUAL_STRING("Today \xc2\xa0Today", contentData.characters.c_str());
}

/* more ENTITY tests */
static void testBigEntity(void)
{
    WbxmlParser parser(0);
    DomTestHandler handler;
    parser.setContentHandler(&handler);
    ASSERT_EQUAL_INT(WBXML_STATUS_OK, parser.parse(big_entity_wbxml,
                sizeof(big_entity_wbxml), true));

    Element &sessionType = handler.root.children[0].children[0].children[0];
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
#endif

extern "C" TestRef ImpsEncoderTest_tests(void)
{
    EMB_UNIT_TESTFIXTURES(fixtures) {
        DEFINE_TEST(testSmokeTest),
        DEFINE_TEST(testSimpleCsp12),
        DEFINE_TEST(testSimpleCsp11Level2),
        DEFINE_TEST(testSimpleContent),
        DEFINE_TEST(testSimpleOpaqueInteger),
        DEFINE_TEST(testSimpleOpaqueDatetime),
        DEFINE_TEST(testSimpleSwitchPage),
        DEFINE_TEST(testCapRequest),
        DEFINE_TEST(testEncodeInChunk),
        DEFINE_TEST(testImpsEncoderSmokeTest),
        DEFINE_TEST(testImpsEncoderReset),
        DEFINE_TEST(testImpsStringContentData),
        DEFINE_TEST(testImpsOpaqueContentData),
    };
    EMB_UNIT_TESTCALLER(ImpsEncoderTest, "ImpsEncoderTest", setUp, tearDown, fixtures);

    // temporary work around for the linker/loader problem of the sooner build
    static TestCaller t = ImpsEncoderTest;
    t.isa = (TestImplement *)&TestCallerImplement;
    return (TestRef)&t;
}

