/* C code produced by gperf version 3.0.1 */
/* Command-line: gperf -G -C -F ,0 -t -NfindTag gperf/csp13tags.gperf  */
/* Computed positions: -k'1-2,4,6,8,11,14,$' */

#if !((' ' == 32) && ('!' == 33) && ('"' == 34) && ('#' == 35) \
      && ('%' == 37) && ('&' == 38) && ('\'' == 39) && ('(' == 40) \
      && (')' == 41) && ('*' == 42) && ('+' == 43) && (',' == 44) \
      && ('-' == 45) && ('.' == 46) && ('/' == 47) && ('0' == 48) \
      && ('1' == 49) && ('2' == 50) && ('3' == 51) && ('4' == 52) \
      && ('5' == 53) && ('6' == 54) && ('7' == 55) && ('8' == 56) \
      && ('9' == 57) && (':' == 58) && (';' == 59) && ('<' == 60) \
      && ('=' == 61) && ('>' == 62) && ('?' == 63) && ('A' == 65) \
      && ('B' == 66) && ('C' == 67) && ('D' == 68) && ('E' == 69) \
      && ('F' == 70) && ('G' == 71) && ('H' == 72) && ('I' == 73) \
      && ('J' == 74) && ('K' == 75) && ('L' == 76) && ('M' == 77) \
      && ('N' == 78) && ('O' == 79) && ('P' == 80) && ('Q' == 81) \
      && ('R' == 82) && ('S' == 83) && ('T' == 84) && ('U' == 85) \
      && ('V' == 86) && ('W' == 87) && ('X' == 88) && ('Y' == 89) \
      && ('Z' == 90) && ('[' == 91) && ('\\' == 92) && (']' == 93) \
      && ('^' == 94) && ('_' == 95) && ('a' == 97) && ('b' == 98) \
      && ('c' == 99) && ('d' == 100) && ('e' == 101) && ('f' == 102) \
      && ('g' == 103) && ('h' == 104) && ('i' == 105) && ('j' == 106) \
      && ('k' == 107) && ('l' == 108) && ('m' == 109) && ('n' == 110) \
      && ('o' == 111) && ('p' == 112) && ('q' == 113) && ('r' == 114) \
      && ('s' == 115) && ('t' == 116) && ('u' == 117) && ('v' == 118) \
      && ('w' == 119) && ('x' == 120) && ('y' == 121) && ('z' == 122) \
      && ('{' == 123) && ('|' == 124) && ('}' == 125) && ('~' == 126))
/* The character set is not based on ISO-646.  */
error "gperf generated tables don't work with this execution character set. Please report a bug to <bug-gnu-gperf@gnu.org>."
#endif

#line 1 "gperf/csp13tags.gperf"

/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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

#include <string.h>
#include "csp13_hash.h"

#line 23 "gperf/csp13tags.gperf"
struct TagEntry {
    const char * name;
    int key;
};

#define TOTAL_KEYWORDS 442
#define MIN_WORD_LENGTH 2
#define MAX_WORD_LENGTH 32
#define MIN_HASH_VALUE 9
#define MAX_HASH_VALUE 1542
/* maximum key range = 1534, duplicates = 0 */

#ifdef __GNUC__
__inline
#else
#ifdef __cplusplus
inline
#endif
#endif
static unsigned int
hash (str, len)
     register const char *str;
     register unsigned int len;
{
  static const unsigned short asso_values[] =
    {
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543,  300, 1543, 1543, 1543,   20,
        10, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543,   25,   50,   40,    5,  495,
       130,  125,   35,  145,  385,    0,   35,  215,  305,   20,
        15,    5,   90,  285,  350,    0,  365,  270,    5, 1543,
         5, 1543, 1543, 1543, 1543,   10, 1543,   15,  435,   35,
       165,    0,   50,  360,  210,    0, 1543,  190,   20,  160,
        10,    5,  260,  320,    0,    5,    0,  105,  365,   75,
        80,  260,    0, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543, 1543,
      1543, 1543, 1543, 1543, 1543, 1543
    };
  register int hval = len;

  switch (hval)
    {
      default:
        hval += asso_values[(unsigned char)str[13]];
      /*FALLTHROUGH*/
      case 13:
      case 12:
      case 11:
        hval += asso_values[(unsigned char)str[10]];
      /*FALLTHROUGH*/
      case 10:
      case 9:
      case 8:
        hval += asso_values[(unsigned char)str[7]];
      /*FALLTHROUGH*/
      case 7:
      case 6:
        hval += asso_values[(unsigned char)str[5]];
      /*FALLTHROUGH*/
      case 5:
      case 4:
        hval += asso_values[(unsigned char)str[3]];
      /*FALLTHROUGH*/
      case 3:
      case 2:
        hval += asso_values[(unsigned char)str[1]];
      /*FALLTHROUGH*/
      case 1:
        hval += asso_values[(unsigned char)str[0]];
        break;
    }
  return hval + asso_values[(unsigned char)str[len - 1]];
}

static const struct TagEntry wordlist[] =
  {
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0},
#line 81 "gperf/csp13tags.gperf"
    {"User", 0x0039},
    {"",0}, {"",0}, {"",0},
#line 83 "gperf/csp13tags.gperf"
    {"UserList", 0x003b},
#line 307 "gperf/csp13tags.gperf"
    {"Zone", 0x0535},
#line 366 "gperf/csp13tags.gperf"
    {"Users", 0x071f},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 446 "gperf/csp13tags.gperf"
    {"UserIDList", 0x093f},
#line 82 "gperf/csp13tags.gperf"
    {"UserID", 0x003a},
    {"",0},
#line 279 "gperf/csp13tags.gperf"
    {"DirectContent", 0x0519},
    {"",0}, {"",0}, {"",0},
#line 213 "gperf/csp13tags.gperf"
    {"UDPPort", 0x0313},
#line 41 "gperf/csp13tags.gperf"
    {"DateTime", 0x0011},
    {"",0}, {"",0}, {"",0},
#line 237 "gperf/csp13tags.gperf"
    {"DeleteAttributeList-Request", 0x040c},
    {"",0}, {"",0},
#line 444 "gperf/csp13tags.gperf"
    {"UserIDPair", 0x093d},
    {"",0}, {"",0},
#line 58 "gperf/csp13tags.gperf"
    {"Presence", 0x0022},
#line 356 "gperf/csp13tags.gperf"
    {"Left", 0x0715},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 207 "gperf/csp13tags.gperf"
    {"ParserSize", 0x030d},
#line 454 "gperf/csp13tags.gperf"
    {"PairID", 0x0a0c},
#line 251 "gperf/csp13tags.gperf"
    {"PresenceAuth-User", 0x041a},
    {"",0},
#line 35 "gperf/csp13tags.gperf"
    {"Code", 0x000b},
    {"",0},
#line 392 "gperf/csp13tags.gperf"
    {"Domain", 0x0906},
    {"",0},
#line 443 "gperf/csp13tags.gperf"
    {"UnrecognizedUserID", 0x093c},
#line 49 "gperf/csp13tags.gperf"
    {"Logo", 0x0019},
#line 403 "gperf/csp13tags.gperf"
    {"Color", 0x0913},
    {"",0},
#line 182 "gperf/csp13tags.gperf"
    {"PresenceFeat", 0x022f},
#line 252 "gperf/csp13tags.gperf"
    {"PresenceNotification-Request", 0x041b},
    {"",0},
#line 96 "gperf/csp13tags.gperf"
    {"Disconnect", 0x0110},
    {"",0}, {"",0},
#line 60 "gperf/csp13tags.gperf"
    {"PresenceValue", 0x0024},
#line 57 "gperf/csp13tags.gperf"
    {"Poll", 0x0021},
    {"",0},
#line 377 "gperf/csp13tags.gperf"
    {"UserMapList", 0x072a},
    {"",0}, {"",0}, {"",0},
#line 262 "gperf/csp13tags.gperf"
    {"Alias", 0x0508},
#line 42 "gperf/csp13tags.gperf"
    {"Description", 0x0012},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 34 "gperf/csp13tags.gperf"
    {"ClientID", 0x000a},
    {"",0},
#line 29 "gperf/csp13tags.gperf"
    {"Acceptance", 0x0005},
    {"",0},
#line 232 "gperf/csp13tags.gperf"
    {"CreateAttributeList-Request", 0x0407},
#line 270 "gperf/csp13tags.gperf"
    {"ClientVersion", 0x0510},
    {"",0},
#line 267 "gperf/csp13tags.gperf"
    {"ClientInfo", 0x050d},
#line 236 "gperf/csp13tags.gperf"
    {"DefaultList", 0x040b},
#line 303 "gperf/csp13tags.gperf"
    {"Contact", 0x0531},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 203 "gperf/csp13tags.gperf"
    {"AnyContent", 0x0309},
    {"",0}, {"",0},
#line 235 "gperf/csp13tags.gperf"
    {"DefaultContactList", 0x040a},
    {"",0}, {"",0},
#line 37 "gperf/csp13tags.gperf"
    {"ContentData", 0x000d},
#line 149 "gperf/csp13tags.gperf"
    {"ContListFunc", 0x020c},
    {"",0},
#line 181 "gperf/csp13tags.gperf"
    {"PresenceDeliverFunc", 0x022e},
#line 64 "gperf/csp13tags.gperf"
    {"RemoveList", 0x0028},
    {"",0},
#line 290 "gperf/csp13tags.gperf"
    {"PreferredContacts", 0x0524},
    {"",0},
#line 63 "gperf/csp13tags.gperf"
    {"Recipient", 0x0027},
#line 289 "gperf/csp13tags.gperf"
    {"PrefC", 0x0523},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 312 "gperf/csp13tags.gperf"
    {"ClientContentLimit", 0x053b},
#line 90 "gperf/csp13tags.gperf"
    {"CapabilityList", 0x010a},
    {"",0}, {"",0}, {"",0},
#line 229 "gperf/csp13tags.gperf"
    {"ContentPolicyLimit", 0x0324},
    {"",0},
#line 194 "gperf/csp13tags.gperf"
    {"UPDPR", 0x023b},
#line 36 "gperf/csp13tags.gperf"
    {"ContactList", 0x000c},
    {"",0},
#line 79 "gperf/csp13tags.gperf"
    {"URL", 0x0037},
    {"",0}, {"",0}, {"",0},
#line 294 "gperf/csp13tags.gperf"
    {"Registration", 0x0528},
#line 359 "gperf/csp13tags.gperf"
    {"OwnProperties", 0x0718},
    {"",0}, {"",0}, {"",0},
#line 80 "gperf/csp13tags.gperf"
    {"URLList", 0x0038},
    {"",0},
#line 400 "gperf/csp13tags.gperf"
    {"Font", 0x0910},
#line 250 "gperf/csp13tags.gperf"
    {"PresenceAuth-Request", 0x0419},
    {"",0}, {"",0}, {"",0},
#line 65 "gperf/csp13tags.gperf"
    {"RemoveNickList", 0x0029},
    {"",0},
#line 231 "gperf/csp13tags.gperf"
    {"ContactListProperties", 0x0406},
#line 249 "gperf/csp13tags.gperf"
    {"UnsubscribePresence-Request", 0x0418},
    {"",0},
#line 89 "gperf/csp13tags.gperf"
    {"CancelInviteUser-Request", 0x0108},
    {"",0},
#line 451 "gperf/csp13tags.gperf"
    {"UnsubscribeNotification-Request", 0x0a09},
#line 287 "gperf/csp13tags.gperf"
    {"OnlineStatus", 0x0521},
    {"",0},
#line 316 "gperf/csp13tags.gperf"
    {"BlockList", 0x0605},
    {"",0},
#line 395 "gperf/csp13tags.gperf"
    {"IDList", 0x0909},
    {"",0},
#line 263 "gperf/csp13tags.gperf"
    {"Altitude", 0x0509},
#line 243 "gperf/csp13tags.gperf"
    {"GetPresence-Request", 0x0412},
#line 244 "gperf/csp13tags.gperf"
    {"GetPresence-Response", 0x0413},
#line 420 "gperf/csp13tags.gperf"
    {"InText", 0x0924},
    {"",0},
#line 283 "gperf/csp13tags.gperf"
    {"Latitude", 0x051d},
    {"",0},
#line 48 "gperf/csp13tags.gperf"
    {"InUse", 0x0018},
    {"",0},
#line 469 "gperf/csp13tags.gperf"
    {"ContactListIDList", 0x0b07},
    {"",0},
#line 468 "gperf/csp13tags.gperf"
    {"BlockListInUse", 0x0b06},
#line 101 "gperf/csp13tags.gperf"
    {"InviteNote", 0x0115},
    {"",0}, {"",0},
#line 100 "gperf/csp13tags.gperf"
    {"InviteID", 0x0114},
#line 239 "gperf/csp13tags.gperf"
    {"GetAttributeList-Request", 0x040e},
#line 240 "gperf/csp13tags.gperf"
    {"GetAttributeList-Response", 0x040f},
    {"",0},
#line 305 "gperf/csp13tags.gperf"
    {"Cstatus", 0x0533},
    {"",0},
#line 268 "gperf/csp13tags.gperf"
    {"ClientProducer", 0x050e},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 328 "gperf/csp13tags.gperf"
    {"GrantList", 0x0611},
    {"",0},
#line 180 "gperf/csp13tags.gperf"
    {"PresenceAuthFunc", 0x022d},
    {"",0},
#line 410 "gperf/csp13tags.gperf"
    {"PublicProfile", 0x091a},
#line 62 "gperf/csp13tags.gperf"
    {"Qualifier", 0x0026},
#line 234 "gperf/csp13tags.gperf"
    {"DefaultAttributeList", 0x0409},
#line 280 "gperf/csp13tags.gperf"
    {"FreeTextLocation", 0x051a},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 224 "gperf/csp13tags.gperf"
    {"UserSessionLimit", 0x031f},
    {"",0}, {"",0},
#line 467 "gperf/csp13tags.gperf"
    {"GrantListInUse", 0x0b05},
#line 163 "gperf/csp13tags.gperf"
    {"GLBLU", 0x021c},
#line 66 "gperf/csp13tags.gperf"
    {"Result", 0x002a},
#line 291 "gperf/csp13tags.gperf"
    {"PreferredLanguage", 0x0525},
    {"",0}, {"",0},
#line 341 "gperf/csp13tags.gperf"
    {"Admin", 0x0706},
    {"",0},
#line 260 "gperf/csp13tags.gperf"
    {"Address", 0x0506},
    {"",0}, {"",0},
#line 215 "gperf/csp13tags.gperf"
    {"UDPAddress", 0x0315},
    {"",0},
#line 86 "gperf/csp13tags.gperf"
    {"AllFunctions", 0x0105},
    {"",0},
#line 87 "gperf/csp13tags.gperf"
    {"AllFunctionsRequest", 0x0106},
#line 302 "gperf/csp13tags.gperf"
    {"Cname", 0x0530},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0},
#line 43 "gperf/csp13tags.gperf"
    {"DetailedResult", 0x0013},
#line 265 "gperf/csp13tags.gperf"
    {"Caddr", 0x050b},
#line 281 "gperf/csp13tags.gperf"
    {"GeoLocation", 0x051b},
#line 95 "gperf/csp13tags.gperf"
    {"DigestSchema", 0x010f},
#line 394 "gperf/csp13tags.gperf"
    {"HistoryPeriod", 0x0908},
    {"",0},
#line 88 "gperf/csp13tags.gperf"
    {"CancelInvite-Request", 0x0107},
#line 439 "gperf/csp13tags.gperf"
    {"RequiresResponse", 0x0938},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 30 "gperf/csp13tags.gperf"
    {"AddList", 0x0006},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 285 "gperf/csp13tags.gperf"
    {"Model", 0x051f},
#line 273 "gperf/csp13tags.gperf"
    {"ContactInfo", 0x0513},
#line 381 "gperf/csp13tags.gperf"
    {"MP", 0x0805},
    {"",0}, {"",0},
#line 271 "gperf/csp13tags.gperf"
    {"CommC", 0x0511},
    {"",0}, {"",0}, {"",0},
#line 111 "gperf/csp13tags.gperf"
    {"Logout-Request", 0x011f},
#line 199 "gperf/csp13tags.gperf"
    {"AcceptedCharset", 0x0305},
    {"",0}, {"",0},
#line 255 "gperf/csp13tags.gperf"
    {"AutoSubscribe", 0x041E},
#line 256 "gperf/csp13tags.gperf"
    {"GetReactiveAuthStatus-Request", 0x041f},
#line 257 "gperf/csp13tags.gperf"
    {"GetReactiveAuthStatus-Response", 0x0420},
    {"",0}, {"",0}, {"",0},
#line 201 "gperf/csp13tags.gperf"
    {"AcceptedContentType", 0x0307},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 349 "gperf/csp13tags.gperf"
    {"GroupProperties", 0x070e},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 144 "gperf/csp13tags.gperf"
    {"AttListFunc", 0x0206},
    {"",0},
#line 391 "gperf/csp13tags.gperf"
    {"CIR", 0x0905},
#line 47 "gperf/csp13tags.gperf"
    {"GroupList", 0x0017},
#line 432 "gperf/csp13tags.gperf"
    {"UserNotify", 0x0931},
    {"",0},
#line 91 "gperf/csp13tags.gperf"
    {"CapabilityRequest", 0x010b},
    {"",0},
#line 343 "gperf/csp13tags.gperf"
    {"DeleteGroup-Request", 0x0708},
#line 150 "gperf/csp13tags.gperf"
    {"CREAG", 0x020d},
#line 94 "gperf/csp13tags.gperf"
    {"DigestBytes", 0x010e},
#line 168 "gperf/csp13tags.gperf"
    {"GroupUseFunc", 0x0221},
    {"",0},
#line 401 "gperf/csp13tags.gperf"
    {"Size", 0x0911},
#line 292 "gperf/csp13tags.gperf"
    {"ReferredContent", 0x0526},
#line 298 "gperf/csp13tags.gperf"
    {"Street", 0x052c},
    {"",0}, {"",0},
#line 97 "gperf/csp13tags.gperf"
    {"Functions", 0x0111},
#line 278 "gperf/csp13tags.gperf"
    {"DevManufacturer", 0x0518},
    {"",0},
#line 253 "gperf/csp13tags.gperf"
    {"UpdatePresence-Request", 0x041c},
#line 261 "gperf/csp13tags.gperf"
    {"AddrPref", 0x0507},
    {"",0},
#line 297 "gperf/csp13tags.gperf"
    {"StatusText", 0x052b},
#line 73 "gperf/csp13tags.gperf"
    {"Status", 0x0031},
#line 348 "gperf/csp13tags.gperf"
    {"GroupChangeNotice", 0x070d},
    {"",0}, {"",0},
#line 174 "gperf/csp13tags.gperf"
    {"InviteFunc", 0x0227},
    {"",0},
#line 441 "gperf/csp13tags.gperf"
    {"GroupContentLimit", 0x093a},
#line 295 "gperf/csp13tags.gperf"
    {"StatusContent", 0x0529},
    {"",0},
#line 402 "gperf/csp13tags.gperf"
    {"Style", 0x0912},
    {"",0},
#line 69 "gperf/csp13tags.gperf"
    {"Session", 0x002d},
    {"",0},
#line 306 "gperf/csp13tags.gperf"
    {"Note", 0x0534},
    {"",0},
#line 139 "gperf/csp13tags.gperf"
    {"OtherServer", 0x013c},
    {"",0},
#line 301 "gperf/csp13tags.gperf"
    {"Cap", 0x052f},
    {"",0}, {"",0}, {"",0},
#line 275 "gperf/csp13tags.gperf"
    {"Country", 0x0515},
    {"",0},
#line 54 "gperf/csp13tags.gperf"
    {"Name", 0x001e},
    {"",0}, {"",0},
#line 398 "gperf/csp13tags.gperf"
    {"Watcher", 0x090e},
    {"",0}, {"",0},
#line 269 "gperf/csp13tags.gperf"
    {"ClientType", 0x050f},
    {"",0}, {"",0}, {"",0},
#line 342 "gperf/csp13tags.gperf"
    {"CreateGroup-Request", 0x0707},
#line 175 "gperf/csp13tags.gperf"
    {"MBRAC", 0x0228},
    {"",0}, {"",0}, {"",0},
#line 151 "gperf/csp13tags.gperf"
    {"DCLI", 0x020f},
    {"",0}, {"",0}, {"",0},
#line 228 "gperf/csp13tags.gperf"
    {"ContentPolicy", 0x0323},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 112 "gperf/csp13tags.gperf"
    {"Nonce", 0x0120},
#line 39 "gperf/csp13tags.gperf"
    {"ContentSize", 0x000f},
    {"",0},
#line 109 "gperf/csp13tags.gperf"
    {"Login-Request", 0x011d},
#line 311 "gperf/csp13tags.gperf"
    {"Text", 0x053a},
#line 67 "gperf/csp13tags.gperf"
    {"ScreenName", 0x002b},
    {"",0}, {"",0}, {"",0},
#line 110 "gperf/csp13tags.gperf"
    {"Login-Response", 0x011e},
    {"",0},
#line 205 "gperf/csp13tags.gperf"
    {"InitialDeliveryMethod", 0x030b},
    {"",0},
#line 299 "gperf/csp13tags.gperf"
    {"TimeZone", 0x052d},
#line 102 "gperf/csp13tags.gperf"
    {"Invite-Request", 0x0116},
#line 130 "gperf/csp13tags.gperf"
    {"TimeToLive", 0x0132},
#line 170 "gperf/csp13tags.gperf"
    {"IMFeat", 0x0223},
#line 415 "gperf/csp13tags.gperf"
    {"AuthorizeAndGrant", 0x091f},
#line 414 "gperf/csp13tags.gperf"
    {"ApplicationID", 0x091e},
#line 286 "gperf/csp13tags.gperf"
    {"NamedArea", 0x0520},
    {"",0},
#line 404 "gperf/csp13tags.gperf"
    {"ContentName", 0x0914},
#line 115 "gperf/csp13tags.gperf"
    {"ResponseNote", 0x0123},
    {"",0},
#line 148 "gperf/csp13tags.gperf"
    {"CCLI", 0x020b},
#line 143 "gperf/csp13tags.gperf"
    {"ADDGM", 0x0205},
    {"",0}, {"",0},
#line 113 "gperf/csp13tags.gperf"
    {"Password", 0x0121},
#line 258 "gperf/csp13tags.gperf"
    {"CreateList-Response", 0x0421},
    {"",0}, {"",0},
#line 438 "gperf/csp13tags.gperf"
    {"WatcherCount", 0x0937},
    {"",0},
#line 166 "gperf/csp13tags.gperf"
    {"GroupFeat", 0x021f},
    {"",0}, {"",0},
#line 46 "gperf/csp13tags.gperf"
    {"GroupID", 0x0016},
#line 358 "gperf/csp13tags.gperf"
    {"Mod", 0x0717},
#line 248 "gperf/csp13tags.gperf"
    {"ListManage-Response", 0x0417},
    {"",0},
#line 397 "gperf/csp13tags.gperf"
    {"AnswerOptionText", 0x090b},
#line 417 "gperf/csp13tags.gperf"
    {"ContactListNotify", 0x0921},
#line 413 "gperf/csp13tags.gperf"
    {"AnswerOptions", 0x091d},
    {"",0}, {"",0}, {"",0},
#line 411 "gperf/csp13tags.gperf"
    {"AnswerOption", 0x091b},
    {"",0},
#line 412 "gperf/csp13tags.gperf"
    {"AnswerOptionID", 0x091c},
    {"",0},
#line 445 "gperf/csp13tags.gperf"
    {"ValidUserID", 0x093e},
    {"",0},
#line 77 "gperf/csp13tags.gperf"
    {"TransactionID", 0x0035},
    {"",0}, {"",0},
#line 74 "gperf/csp13tags.gperf"
    {"Transaction", 0x0032},
#line 457 "gperf/csp13tags.gperf"
    {"UpdatePublicProfile-Request", 0x0a0f},
#line 165 "gperf/csp13tags.gperf"
    {"GroupAuthFunc", 0x021e},
    {"",0},
#line 390 "gperf/csp13tags.gperf"
    {"ADVSR", 0x080e},
#line 76 "gperf/csp13tags.gperf"
    {"TransactionDescriptor", 0x0034},
#line 212 "gperf/csp13tags.gperf"
    {"TCPPort", 0x0312},
#line 75 "gperf/csp13tags.gperf"
    {"TransactionContent", 0x0033},
#line 361 "gperf/csp13tags.gperf"
    {"RejectList-Response", 0x071a},
    {"",0},
#line 40 "gperf/csp13tags.gperf"
    {"ContentType", 0x0010},
#line 324 "gperf/csp13tags.gperf"
    {"GetMessageList-Request", 0x060d},
#line 325 "gperf/csp13tags.gperf"
    {"GetMessageList-Response", 0x060e},
#line 310 "gperf/csp13tags.gperf"
    {"Link", 0x0539},
#line 146 "gperf/csp13tags.gperf"
    {"CAAUT", 0x0208},
#line 449 "gperf/csp13tags.gperf"
    {"VersionList", 0x0a07},
    {"",0}, {"",0},
#line 277 "gperf/csp13tags.gperf"
    {"Crossing2", 0x0517},
#line 104 "gperf/csp13tags.gperf"
    {"InviteType", 0x0118},
#line 333 "gperf/csp13tags.gperf"
    {"RejectMessage-Request", 0x0616},
#line 322 "gperf/csp13tags.gperf"
    {"GetBlockedList-Request", 0x060b},
#line 323 "gperf/csp13tags.gperf"
    {"GetBlockedList-Response", 0x060c},
#line 274 "gperf/csp13tags.gperf"
    {"ContainedvCard", 0x0514},
    {"",0},
#line 380 "gperf/csp13tags.gperf"
    {"LeftBlocked", 0x072d},
    {"",0},
#line 282 "gperf/csp13tags.gperf"
    {"Language", 0x051c},
#line 276 "gperf/csp13tags.gperf"
    {"Crossing1", 0x0516},
    {"",0},
#line 453 "gperf/csp13tags.gperf"
    {"AdvancedCriteria", 0x0a0b},
#line 337 "gperf/csp13tags.gperf"
    {"DeliveryTime", 0x061a},
    {"",0}, {"",0},
#line 452 "gperf/csp13tags.gperf"
    {"Notification-Request", 0x0a0a},
    {"",0}, {"",0}, {"",0},
#line 458 "gperf/csp13tags.gperf"
    {"DropSegment-Request", 0x0a10},
    {"",0}, {"",0}, {"",0},
#line 293 "gperf/csp13tags.gperf"
    {"ReferredvCard", 0x0527},
#line 191 "gperf/csp13tags.gperf"
    {"SRCH", 0x0238},
    {"",0}, {"",0},
#line 321 "gperf/csp13tags.gperf"
    {"ForwardMessage-Request", 0x060a},
#line 339 "gperf/csp13tags.gperf"
    {"ForwardMessage-Response", 0x0621},
#line 71 "gperf/csp13tags.gperf"
    {"SessionID", 0x002f},
    {"",0},
#line 68 "gperf/csp13tags.gperf"
    {"Sender", 0x002c},
    {"",0},
#line 217 "gperf/csp13tags.gperf"
    {"AcceptedPushLength", 0x0317},
#line 155 "gperf/csp13tags.gperf"
    {"GCLI", 0x0214},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 197 "gperf/csp13tags.gperf"
    {"MG", 0x023e},
    {"",0},
#line 384 "gperf/csp13tags.gperf"
    {"VRID", 0x0808},
#line 296 "gperf/csp13tags.gperf"
    {"StatusMood", 0x052a},
    {"",0}, {"",0},
#line 216 "gperf/csp13tags.gperf"
    {"AcceptedPullLength", 0x0316},
#line 334 "gperf/csp13tags.gperf"
    {"SendMessage-Request", 0x0617},
#line 335 "gperf/csp13tags.gperf"
    {"SendMessage-Response", 0x0618},
#line 200 "gperf/csp13tags.gperf"
    {"AcceptedContentLength", 0x0306},
#line 196 "gperf/csp13tags.gperf"
    {"MF", 0x023d},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 106 "gperf/csp13tags.gperf"
    {"InviteUser-Response", 0x011a},
#line 59 "gperf/csp13tags.gperf"
    {"PresenceSubList", 0x0023},
    {"",0},
#line 272 "gperf/csp13tags.gperf"
    {"CommCap", 0x0512},
#line 140 "gperf/csp13tags.gperf"
    {"PresenceAttributeNSName", 0x013d},
    {"",0},
#line 85 "gperf/csp13tags.gperf"
    {"Value", 0x003d},
    {"",0}, {"",0},
#line 405 "gperf/csp13tags.gperf"
    {"Map", 0x0915},
    {"",0},
#line 45 "gperf/csp13tags.gperf"
    {"Group", 0x0015},
#line 242 "gperf/csp13tags.gperf"
    {"GetList-Response", 0x0411},
    {"",0}, {"",0}, {"",0},
#line 218 "gperf/csp13tags.gperf"
    {"AcceptedRichContentLength", 0x0318},
#line 133 "gperf/csp13tags.gperf"
    {"ReceiveList", 0x0136},
    {"",0},
#line 55 "gperf/csp13tags.gperf"
    {"NickList", 0x001f},
    {"",0}, {"",0},
#line 120 "gperf/csp13tags.gperf"
    {"SearchLimit", 0x0128},
    {"",0}, {"",0},
#line 466 "gperf/csp13tags.gperf"
    {"SegmentContent", 0x0a18},
    {"",0}, {"",0}, {"",0},
#line 118 "gperf/csp13tags.gperf"
    {"SearchID", 0x0126},
    {"",0}, {"",0}, {"",0},
#line 421 "gperf/csp13tags.gperf"
    {"SegmentCount", 0x0925},
#line 56 "gperf/csp13tags.gperf"
    {"NickName", 0x0020},
#line 117 "gperf/csp13tags.gperf"
    {"SearchFindings", 0x0125},
#line 465 "gperf/csp13tags.gperf"
    {"SearchPair", 0x0a17},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 103 "gperf/csp13tags.gperf"
    {"Invite-Response", 0x0117},
    {"",0},
#line 125 "gperf/csp13tags.gperf"
    {"SearchResult", 0x012d},
#line 116 "gperf/csp13tags.gperf"
    {"SearchElement", 0x0124},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 92 "gperf/csp13tags.gperf"
    {"ClientCapability-Request", 0x010c},
#line 93 "gperf/csp13tags.gperf"
    {"ClientCapability-Response", 0x010d},
    {"",0}, {"",0},
#line 128 "gperf/csp13tags.gperf"
    {"SessionCookie", 0x0130},
#line 319 "gperf/csp13tags.gperf"
    {"DeliveryReport", 0x0608},
    {"",0},
#line 362 "gperf/csp13tags.gperf"
    {"RemoveGroupMembers-Request", 0x071b},
#line 371 "gperf/csp13tags.gperf"
    {"GetJoinedUsers-Request", 0x0724},
#line 372 "gperf/csp13tags.gperf"
    {"GetJoinedUsers-Response", 0x0725},
    {"",0},
#line 219 "gperf/csp13tags.gperf"
    {"AcceptedTextContentLength", 0x0319},
    {"",0},
#line 320 "gperf/csp13tags.gperf"
    {"DeliveryReport-Request", 0x0609},
#line 309 "gperf/csp13tags.gperf"
    {"InfoLink", 0x0538},
    {"",0},
#line 332 "gperf/csp13tags.gperf"
    {"NewMessage", 0x0615},
#line 313 "gperf/csp13tags.gperf"
    {"ClientIMPriority", 0x053c},
    {"",0},
#line 308 "gperf/csp13tags.gperf"
    {"Inf_link", 0x0537},
#line 416 "gperf/csp13tags.gperf"
    {"ChosenOptionID", 0x0920},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 122 "gperf/csp13tags.gperf"
    {"SearchPairList", 0x012a},
#line 164 "gperf/csp13tags.gperf"
    {"GRCHN", 0x021d},
    {"",0}, {"",0}, {"",0},
#line 266 "gperf/csp13tags.gperf"
    {"City", 0x050c},
#line 78 "gperf/csp13tags.gperf"
    {"TransactionMode", 0x0036},
#line 434 "gperf/csp13tags.gperf"
    {"VerificationMechanism", 0x0933},
    {"",0}, {"",0},
#line 304 "gperf/csp13tags.gperf"
    {"Cpriority", 0x0532},
    {"",0},
#line 424 "gperf/csp13tags.gperf"
    {"SegmentReference", 0x0928},
    {"",0}, {"",0},
#line 284 "gperf/csp13tags.gperf"
    {"Longitude", 0x051e},
    {"",0},
#line 31 "gperf/csp13tags.gperf"
    {"AddNickList", 0x0007},
    {"",0},
#line 351 "gperf/csp13tags.gperf"
    {"JoinedRequest", 0x0710},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 409 "gperf/csp13tags.gperf"
    {"ClearPublicProfile", 0x0919},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 222 "gperf/csp13tags.gperf"
    {"SessionPriority", 0x031c},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 211 "gperf/csp13tags.gperf"
    {"TCPAddress", 0x0311},
    {"",0}, {"",0}, {"",0},
#line 455 "gperf/csp13tags.gperf"
    {"GetPublicProfile-Request", 0x0a0d},
#line 456 "gperf/csp13tags.gperf"
    {"GetPublicProfile-Response", 0x0a0e},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 179 "gperf/csp13tags.gperf"
    {"NOTIF", 0x022c},
#line 406 "gperf/csp13tags.gperf"
    {"NotificationType", 0x0916},
    {"",0}, {"",0},
#line 318 "gperf/csp13tags.gperf"
    {"DeliveryMethod", 0x0607},
#line 407 "gperf/csp13tags.gperf"
    {"NotificationTypeList", 0x0917},
    {"",0},
#line 70 "gperf/csp13tags.gperf"
    {"SessionDescriptor", 0x002e},
    {"",0},
#line 422 "gperf/csp13tags.gperf"
    {"SegmentID", 0x0926},
    {"",0}, {"",0}, {"",0},
#line 418 "gperf/csp13tags.gperf"
    {"DefaultNotify", 0x0922},
#line 214 "gperf/csp13tags.gperf"
    {"CIRHTTPAddress", 0x0314},
#line 153 "gperf/csp13tags.gperf"
    {"FundamentalFeat", 0x0211},
#line 423 "gperf/csp13tags.gperf"
    {"SegmentInfo", 0x0927},
    {"",0},
#line 399 "gperf/csp13tags.gperf"
    {"WatcherStatus", 0x090f},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 141 "gperf/csp13tags.gperf"
    {"SessionNSName", 0x013e},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 50 "gperf/csp13tags.gperf"
    {"MessageCount", 0x001a},
#line 238 "gperf/csp13tags.gperf"
    {"DeleteList-Request", 0x040d},
    {"",0},
#line 187 "gperf/csp13tags.gperf"
    {"SearchFunc", 0x0234},
#line 134 "gperf/csp13tags.gperf"
    {"VerifyID-Request", 0x0137},
#line 198 "gperf/csp13tags.gperf"
    {"MM", 0x023f},
    {"",0}, {"",0},
#line 186 "gperf/csp13tags.gperf"
    {"RMVGM", 0x0233},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 72 "gperf/csp13tags.gperf"
    {"SessionType", 0x0030},
    {"",0},
#line 108 "gperf/csp13tags.gperf"
    {"KeepAliveTime", 0x011c},
    {"",0},
#line 433 "gperf/csp13tags.gperf"
    {"VerificationKey", 0x0932},
    {"",0},
#line 245 "gperf/csp13tags.gperf"
    {"GetWatcherList-Request", 0x0414},
#line 246 "gperf/csp13tags.gperf"
    {"GetWatcherList-Response", 0x0415},
#line 288 "gperf/csp13tags.gperf"
    {"PLMN", 0x0522},
#line 209 "gperf/csp13tags.gperf"
    {"SupportedBearer", 0x030f},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0},
#line 202 "gperf/csp13tags.gperf"
    {"AcceptedTransferEncoding", 0x0308},
#line 172 "gperf/csp13tags.gperf"
    {"IMSendFunc", 0x0225},
#line 119 "gperf/csp13tags.gperf"
    {"SearchIndex", 0x0127},
    {"",0}, {"",0}, {"",0},
#line 431 "gperf/csp13tags.gperf"
    {"TryAgainTimeout", 0x0930},
    {"",0},
#line 373 "gperf/csp13tags.gperf"
    {"AdminMapList", 0x0726},
    {"",0},
#line 327 "gperf/csp13tags.gperf"
    {"GetMessage-Response", 0x0610},
    {"",0}, {"",0},
#line 385 "gperf/csp13tags.gperf"
    {"VerifyIDFunc", 0x0809},
    {"",0}, {"",0},
#line 114 "gperf/csp13tags.gperf"
    {"Polling-Request", 0x0122},
    {"",0}, {"",0},
#line 233 "gperf/csp13tags.gperf"
    {"CreateList-Request", 0x0408},
    {"",0}, {"",0},
#line 221 "gperf/csp13tags.gperf"
    {"PlainTextCharset", 0x031b},
    {"",0},
#line 344 "gperf/csp13tags.gperf"
    {"GetGroupMembers-Request", 0x0709},
#line 345 "gperf/csp13tags.gperf"
    {"GetGroupMembers-Response", 0x070a},
#line 206 "gperf/csp13tags.gperf"
    {"MultiTrans", 0x030c},
    {"",0}, {"",0},
#line 247 "gperf/csp13tags.gperf"
    {"ListManage-Request", 0x0416},
#line 123 "gperf/csp13tags.gperf"
    {"Search-Request", 0x012b},
    {"",0}, {"",0},
#line 142 "gperf/csp13tags.gperf"
    {"TransactionNSName", 0x013f},
#line 259 "gperf/csp13tags.gperf"
    {"Accuracy", 0x0505},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0},
#line 352 "gperf/csp13tags.gperf"
    {"JoinGroup-Request", 0x0711},
    {"",0}, {"",0},
#line 152 "gperf/csp13tags.gperf"
    {"DELGR", 0x0210},
    {"",0},
#line 223 "gperf/csp13tags.gperf"
    {"SupportedOfflineBearer", 0x031d},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 408 "gperf/csp13tags.gperf"
    {"FriendlyName", 0x0918},
#line 360 "gperf/csp13tags.gperf"
    {"RejectList-Request", 0x0719},
    {"",0},
#line 160 "gperf/csp13tags.gperf"
    {"GETPR", 0x0219},
    {"",0}, {"",0},
#line 425 "gperf/csp13tags.gperf"
    {"SystemMessage", 0x0929},
    {"",0},
#line 52 "gperf/csp13tags.gperf"
    {"MessageURI", 0x001c},
#line 350 "gperf/csp13tags.gperf"
    {"Joined", 0x070f},
    {"",0}, {"",0},
#line 51 "gperf/csp13tags.gperf"
    {"MessageID", 0x001b},
#line 147 "gperf/csp13tags.gperf"
    {"CAINV", 0x0209},
    {"",0},
#line 470 "gperf/csp13tags.gperf"
    {"AnswerOptionsText", 0x0b08},
    {"",0}, {"",0},
#line 145 "gperf/csp13tags.gperf"
    {"BLENT", 0x0207},
#line 330 "gperf/csp13tags.gperf"
    {"MessageInfo", 0x0613},
    {"",0}, {"",0}, {"",0},
#line 338 "gperf/csp13tags.gperf"
    {"MessageInfoList", 0x0620},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 32 "gperf/csp13tags.gperf"
    {"SName", 0x0008},
    {"",0}, {"",0},
#line 167 "gperf/csp13tags.gperf"
    {"GroupMgmtFunc", 0x0220},
#line 396 "gperf/csp13tags.gperf"
    {"MaxWatcherList", 0x090a},
    {"",0}, {"",0}, {"",0},
#line 340 "gperf/csp13tags.gperf"
    {"AddGroupMembers-Request", 0x0705},
    {"",0},
#line 157 "gperf/csp13tags.gperf"
    {"GETGP", 0x0216},
#line 329 "gperf/csp13tags.gperf"
    {"MessageDelivered", 0x0612},
    {"",0}, {"",0}, {"",0},
#line 192 "gperf/csp13tags.gperf"
    {"STSRC", 0x0239},
    {"",0},
#line 427 "gperf/csp13tags.gperf"
    {"SystemMessageList", 0x092b},
    {"",0},
#line 368 "gperf/csp13tags.gperf"
    {"JoinGroup", 0x0721},
#line 44 "gperf/csp13tags.gperf"
    {"EntityList", 0x0014},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 367 "gperf/csp13tags.gperf"
    {"WelcomeNote", 0x0720},
#line 419 "gperf/csp13tags.gperf"
    {"ExtendConversationUser", 0x0923},
    {"",0}, {"",0},
#line 437 "gperf/csp13tags.gperf"
    {"ExtendConversationID", 0x0936},
#line 460 "gperf/csp13tags.gperf"
    {"ExtendConversation-Request", 0x0a12},
#line 459 "gperf/csp13tags.gperf"
    {"ExtendConversation-Response", 0x0a11},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 189 "gperf/csp13tags.gperf"
    {"SETD", 0x0236},
    {"",0}, {"",0}, {"",0},
#line 105 "gperf/csp13tags.gperf"
    {"InviteUser-Request", 0x0119},
    {"",0},
#line 357 "gperf/csp13tags.gperf"
    {"MemberAccess-Request", 0x0716},
    {"",0}, {"",0},
#line 61 "gperf/csp13tags.gperf"
    {"Property", 0x0025},
    {"",0},
#line 241 "gperf/csp13tags.gperf"
    {"GetList-Request", 0x0410},
    {"",0}, {"",0}, {"",0},
#line 435 "gperf/csp13tags.gperf"
    {"GetMap-Request", 0x0934},
#line 204 "gperf/csp13tags.gperf"
    {"DefaultLanguage", 0x030a},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 154 "gperf/csp13tags.gperf"
    {"FWMSG", 0x0212},
    {"",0},
#line 98 "gperf/csp13tags.gperf"
    {"GetSPInfo-Request", 0x0112},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 169 "gperf/csp13tags.gperf"
    {"IMAuthFunc", 0x0222},
    {"",0}, {"",0}, {"",0},
#line 176 "gperf/csp13tags.gperf"
    {"MCLS", 0x0229},
#line 388 "gperf/csp13tags.gperf"
    {"EXCON", 0x080c},
#line 428 "gperf/csp13tags.gperf"
    {"SystemMessageResponse", 0x092c},
    {"",0}, {"",0}, {"",0},
#line 429 "gperf/csp13tags.gperf"
    {"SystemMessageResponseList", 0x092d},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 346 "gperf/csp13tags.gperf"
    {"GetGroupProps-Request", 0x070b},
#line 347 "gperf/csp13tags.gperf"
    {"GetGroupProps-Response", 0x070c},
    {"",0}, {"",0},
#line 184 "gperf/csp13tags.gperf"
    {"REJCM", 0x0231},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 230 "gperf/csp13tags.gperf"
    {"CancelAuth-Request", 0x0405},
#line 355 "gperf/csp13tags.gperf"
    {"LeaveGroup-Response", 0x0714},
#line 254 "gperf/csp13tags.gperf"
    {"SubscribePresence-Request", 0x041d},
    {"",0},
#line 107 "gperf/csp13tags.gperf"
    {"KeepAlive-Request", 0x011b},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 124 "gperf/csp13tags.gperf"
    {"Search-Response", 0x012c},
    {"",0},
#line 375 "gperf/csp13tags.gperf"
    {"Mapping", 0x0728},
#line 315 "gperf/csp13tags.gperf"
    {"MaxPushLength", 0x053e},
    {"",0}, {"",0},
#line 386 "gperf/csp13tags.gperf"
    {"GETMAP", 0x080a},
    {"",0},
#line 353 "gperf/csp13tags.gperf"
    {"JoinGroup-Response", 0x0712},
    {"",0},
#line 158 "gperf/csp13tags.gperf"
    {"GETLM", 0x0217},
    {"",0},
#line 131 "gperf/csp13tags.gperf"
    {"SearchString", 0x0133},
#line 171 "gperf/csp13tags.gperf"
    {"IMReceiveFunc", 0x0224},
#line 317 "gperf/csp13tags.gperf"
    {"BlockEntity-Request", 0x0606},
    {"",0}, {"",0}, {"",0},
#line 314 "gperf/csp13tags.gperf"
    {"MaxPullLength", 0x053d},
    {"",0},
#line 426 "gperf/csp13tags.gperf"
    {"SystemMessageID", 0x092a},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 208 "gperf/csp13tags.gperf"
    {"ServerPollMin", 0x030e},
    {"",0}, {"",0},
#line 188 "gperf/csp13tags.gperf"
    {"ServiceFunc", 0x0235},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 264 "gperf/csp13tags.gperf"
    {"Building", 0x050a},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 84 "gperf/csp13tags.gperf"
    {"Validity", 0x003c},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 450 "gperf/csp13tags.gperf"
    {"SubscribeNotification-Request", 0x0a08},
    {"",0},
#line 369 "gperf/csp13tags.gperf"
    {"SubscribeNotification", 0x0722},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 135 "gperf/csp13tags.gperf"
    {"Extended-Request", 0x0138},
    {"",0}, {"",0},
#line 33 "gperf/csp13tags.gperf"
    {"WV-CSP-Message", 0x0009},
#line 190 "gperf/csp13tags.gperf"
    {"SETGP", 0x0237},
    {"",0},
#line 136 "gperf/csp13tags.gperf"
    {"Extended-Response", 0x0139},
    {"",0}, {"",0},
#line 162 "gperf/csp13tags.gperf"
    {"GETWL", 0x021b},
    {"",0},
#line 138 "gperf/csp13tags.gperf"
    {"ExtendedData", 0x013b},
    {"",0}, {"",0},
#line 226 "gperf/csp13tags.gperf"
    {"MultiTransPerMessage", 0x0321},
    {"",0}, {"",0},
#line 225 "gperf/csp13tags.gperf"
    {"CIRSMSAddress", 0x0320},
    {"",0},
#line 38 "gperf/csp13tags.gperf"
    {"ContentEncoding", 0x000e},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0},
#line 331 "gperf/csp13tags.gperf"
    {"MessageNotification", 0x0614},
#line 173 "gperf/csp13tags.gperf"
    {"INVIT", 0x0226},
    {"",0}, {"",0},
#line 379 "gperf/csp13tags.gperf"
    {"JoinedBlocked", 0x072c},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 227 "gperf/csp13tags.gperf"
    {"OnlineETEMHandling", 0x0322},
    {"",0},
#line 336 "gperf/csp13tags.gperf"
    {"SetDeliveryMethod-Request", 0x0619},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 156 "gperf/csp13tags.gperf"
    {"GETGM", 0x0215},
    {"",0},
#line 442 "gperf/csp13tags.gperf"
    {"MessageTotalCount", 0x093b},
    {"",0}, {"",0},
#line 436 "gperf/csp13tags.gperf"
    {"GetMap-Response", 0x0935},
    {"",0}, {"",0},
#line 99 "gperf/csp13tags.gperf"
    {"GetSPInfo-Response", 0x0113},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 210 "gperf/csp13tags.gperf"
    {"SupportedCIRMethod", 0x0310},
    {"",0},
#line 183 "gperf/csp13tags.gperf"
    {"REACT", 0x0230},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0},
#line 177 "gperf/csp13tags.gperf"
    {"MDELIV", 0x022a},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 326 "gperf/csp13tags.gperf"
    {"GetMessage-Request", 0x060f},
    {"",0}, {"",0},
#line 363 "gperf/csp13tags.gperf"
    {"SetGroupProps-Request", 0x071c},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0},
#line 383 "gperf/csp13tags.gperf"
    {"GETJU", 0x0807},
#line 378 "gperf/csp13tags.gperf"
    {"UserMapping", 0x072b},
    {"",0},
#line 121 "gperf/csp13tags.gperf"
    {"KeepAlive-Response", 0x0129},
    {"",0}, {"",0},
#line 127 "gperf/csp13tags.gperf"
    {"Service-Response", 0x012f},
    {"",0},
#line 393 "gperf/csp13tags.gperf"
    {"ExtBlock", 0x0907},
    {"",0},
#line 137 "gperf/csp13tags.gperf"
    {"AgreedCapabilityList", 0x013a},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 193 "gperf/csp13tags.gperf"
    {"SUBGCN", 0x023a},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 464 "gperf/csp13tags.gperf"
    {"SystemMessage-User", 0x0a16},
    {"",0}, {"",0},
#line 463 "gperf/csp13tags.gperf"
    {"SystemMessage-Request", 0x0a15},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 374 "gperf/csp13tags.gperf"
    {"AdminMapping", 0x0727},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 159 "gperf/csp13tags.gperf"
    {"GETM", 0x0218},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0},
#line 376 "gperf/csp13tags.gperf"
    {"ModMapping", 0x0729},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 387 "gperf/csp13tags.gperf"
    {"SGMNT", 0x080b},
    {"",0}, {"",0},
#line 389 "gperf/csp13tags.gperf"
    {"OFFNOTIF", 0x080d},
#line 195 "gperf/csp13tags.gperf"
    {"WVCSPFeat", 0x023c},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 300 "gperf/csp13tags.gperf"
    {"UserAvailability", 0x052e},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 430 "gperf/csp13tags.gperf"
    {"SystemMessageText", 0x092f},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 462 "gperf/csp13tags.gperf"
    {"GetSegment-Response", 0x0a14},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 447 "gperf/csp13tags.gperf"
    {"WV-CSP-VersionDiscovery-Request", 0x0a05},
#line 448 "gperf/csp13tags.gperf"
    {"WV-CSP-VersionDiscovery-Response", 0x0a06},
#line 370 "gperf/csp13tags.gperf"
    {"SubscribeType", 0x0723},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 364 "gperf/csp13tags.gperf"
    {"SubscribeGroupNotice-Request", 0x071d},
#line 365 "gperf/csp13tags.gperf"
    {"SubscribeGroupNotice-Response", 0x071e},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 185 "gperf/csp13tags.gperf"
    {"REJEC", 0x0232},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 354 "gperf/csp13tags.gperf"
    {"LeaveGroup-Request", 0x0713},
#line 132 "gperf/csp13tags.gperf"
    {"CompletionFlag", 0x0134},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 129 "gperf/csp13tags.gperf"
    {"StopSearch-Request", 0x0131},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0},
#line 220 "gperf/csp13tags.gperf"
    {"OfflineETEMHandling", 0x031a},
    {"",0},
#line 161 "gperf/csp13tags.gperf"
    {"GETSPI", 0x021a},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 178 "gperf/csp13tags.gperf"
    {"NEWM", 0x022b},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 126 "gperf/csp13tags.gperf"
    {"Service-Request", 0x012e},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 382 "gperf/csp13tags.gperf"
    {"GETAUT", 0x0806},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 53 "gperf/csp13tags.gperf"
    {"MSISDN", 0x001d},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 461 "gperf/csp13tags.gperf"
    {"GetSegment-Request", 0x0a13},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 440 "gperf/csp13tags.gperf"
    {"ExtBlockETEM", 0x0939}
  };

#ifdef __GNUC__
__inline
#endif
const struct TagEntry *
findTag (str, len)
     register const char *str;
     register unsigned int len;
{
  if (len <= MAX_WORD_LENGTH && len >= MIN_WORD_LENGTH)
    {
      register int key = hash (str, len);

      if (key <= MAX_HASH_VALUE && key >= 0)
        {
          register const char *s = wordlist[key].name;

          if (*str == *s && !strcmp (str + 1, s + 1))
            return &wordlist[key];
        }
    }
  return 0;
}
#line 471 "gperf/csp13tags.gperf"

int csp13TagNameToKey(const char * str)
{
    const struct TagEntry * tag = findTag(str, strlen(str));
    return tag ? tag->key : -1;
}
