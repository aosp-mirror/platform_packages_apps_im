/* C code produced by gperf version 3.0.2 */
/* Command-line: gperf -G -C -F ,0 -c -t -NfindToken gperf/csp13values.gperf  */
/* Computed positions: -k'1-2,6,$' */

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

#line 1 "gperf/csp13values.gperf"

/*
 * Copyright (C) 2007 Esmertec AG.
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <string.h>
#include "csp13_hash.h"

#line 23 "gperf/csp13values.gperf"
struct TokenEntry {
    const char * name;
    int key;
};

#define TOTAL_KEYWORDS 187
#define MIN_WORD_LENGTH 1
#define MAX_WORD_LENGTH 31
#define MIN_HASH_VALUE 1
#define MAX_HASH_VALUE 477
/* maximum key range = 477, duplicates = 0 */

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
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 100, 478, 478,
      478, 478,   0, 478, 478, 478, 478, 478,   0, 478,
      478, 478, 478, 478, 478,  50,  30,  20,  70,   5,
       35, 115,  70,  45,   0, 478, 145,  40,  40, 145,
        5, 478,  25,   0,  10,  15,  45,  40,  75,  75,
      478, 478, 478, 478, 478,   0, 478,  85, 478,  10,
       35,   0, 478,  40,  15,  80, 478,  30,  30,   0,
        0,  25,  20,   0,  25, 140,  70,   5, 478,  50,
       65,  95, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478, 478, 478, 478, 478,
      478, 478, 478, 478, 478, 478
    };
  register int hval = len;

  switch (hval)
    {
      default:
        hval += asso_values[(unsigned char)str[5]];
      /*FALLTHROUGH*/
      case 5:
      case 4:
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

static const struct TokenEntry wordlist[] =
  {
    {"",0},
#line 66 "gperf/csp13values.gperf"
    {"S", 0x25},
    {"",0}, {"",0},
#line 187 "gperf/csp13values.gperf"
    {"SSMS", 0xA4},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 60 "gperf/csp13values.gperf"
    {"P", 0x1F},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 158 "gperf/csp13values.gperf"
    {"Purple", 0x86},
#line 76 "gperf/csp13values.gperf"
    {"US", 0x2F},
    {"",0},
#line 98 "gperf/csp13values.gperf"
    {"STCP", 0x44},
#line 64 "gperf/csp13values.gperf"
    {"ScreenName", 0x23},
#line 73 "gperf/csp13values.gperf"
    {"T", 0x2C},
    {"",0}, {"",0},
#line 99 "gperf/csp13values.gperf"
    {"SUDP", 0x45},
#line 65 "gperf/csp13values.gperf"
    {"Searchable", 0x24},
#line 198 "gperf/csp13values.gperf"
    {"PP_AGE", 0xAF},
#line 136 "gperf/csp13values.gperf"
    {"JEALOUS", 0x6D},
#line 87 "gperf/csp13values.gperf"
    {"PPU", 0x3A},
#line 181 "gperf/csp13values.gperf"
    {"SENDSTORE", 0x9E},
#line 180 "gperf/csp13values.gperf"
    {"SENDREJECT", 0x9D},
#line 75 "gperf/csp13values.gperf"
    {"U", 0x2E},
#line 205 "gperf/csp13values.gperf"
    {"PP_INTERESTS", 0xB6},
#line 62 "gperf/csp13values.gperf"
    {"Response", 0x21},
    {"",0},
#line 145 "gperf/csp13values.gperf"
    {"Small", 0x79},
#line 59 "gperf/csp13values.gperf"
    {"Public", 0x1E},
#line 202 "gperf/csp13values.gperf"
    {"PP_FREE_TEXT", 0xB3},
#line 101 "gperf/csp13values.gperf"
    {"USER_EMAIL_ADDRESS", 0x47},
    {"",0}, {"",0},
#line 211 "gperf/csp13values.gperf"
    {"C", 0xBC},
#line 67 "gperf/csp13values.gperf"
    {"SC", 0x26},
#line 97 "gperf/csp13values.gperf"
    {"SMS", 0x43},
#line 166 "gperf/csp13values.gperf"
    {"Teal", 0x8E},
    {"",0},
#line 146 "gperf/csp13values.gperf"
    {"Medium", 0x7A},
#line 183 "gperf/csp13values.gperf"
    {"EC", 0xA0},
#line 109 "gperf/csp13values.gperf"
    {"WSP", 0x4F},
    {"",0},
#line 72 "gperf/csp13values.gperf"
    {"Topic", 0x2B},
#line 215 "gperf/csp13values.gperf"
    {"R", 0xC0},
#line 206 "gperf/csp13values.gperf"
    {"PP_MARITAL_STATUS", 0xB7},
    {"",0},
#line 151 "gperf/csp13values.gperf"
    {"Underline", 0x7F},
    {"",0},
#line 179 "gperf/csp13values.gperf"
    {"REJECT", 0x9C},
#line 55 "gperf/csp13values.gperf"
    {"PR", 0x1A},
#line 88 "gperf/csp13values.gperf"
    {"SPA", 0x3B},
    {"",0},
#line 155 "gperf/csp13values.gperf"
    {"White", 0x83},
#line 197 "gperf/csp13values.gperf"
    {"SERVERLOGIC", 0xAE},
#line 133 "gperf/csp13values.gperf"
    {"AP", 0x68},
#line 157 "gperf/csp13values.gperf"
    {"Red", 0x85},
#line 165 "gperf/csp13values.gperf"
    {"Blue", 0x8D},
#line 78 "gperf/csp13values.gperf"
    {"AutoDelete", 0x31},
    {"",0},
#line 85 "gperf/csp13values.gperf"
    {"RequireInvitation", 0x38},
    {"",0},
#line 51 "gperf/csp13values.gperf"
    {"None", 0x16},
#line 102 "gperf/csp13values.gperf"
    {"USER_FIRST_NAME", 0x48},
#line 40 "gperf/csp13values.gperf"
    {"F", 0x0B},
#line 204 "gperf/csp13values.gperf"
    {"PP_INTENTION", 0xB5},
#line 172 "gperf/csp13values.gperf"
    {"USER_FRIENDLY_NAME", 0x95},
#line 176 "gperf/csp13values.gperf"
    {"USER_MARITAL_STATUS", 0x99},
#line 100 "gperf/csp13values.gperf"
    {"USER_ALIAS", 0x46},
#line 201 "gperf/csp13values.gperf"
    {"PP_FRIENDLY_NAME", 0xB2},
    {"",0}, {"",0},
#line 148 "gperf/csp13values.gperf"
    {"Huge", 0x7C},
#line 188 "gperf/csp13values.gperf"
    {"SHTTP", 0xA5},
#line 52 "gperf/csp13values.gperf"
    {"N", 0x17},
#line 175 "gperf/csp13values.gperf"
    {"USER_INTERESTS_HOBBIES", 0x98},
#line 137 "gperf/csp13values.gperf"
    {"MMS", 0x6E},
#line 203 "gperf/csp13values.gperf"
    {"PP_GENDER", 0xB4},
#line 214 "gperf/csp13values.gperf"
    {"PRESENCE_ACCESS", 0xBF},
#line 36 "gperf/csp13values.gperf"
    {"BASE64", 0x07},
#line 186 "gperf/csp13values.gperf"
    {"IC", 0xA3},
#line 35 "gperf/csp13values.gperf"
    {"AutoJoin", 0x06},
#line 96 "gperf/csp13values.gperf"
    {"HTTP", 0x42},
#line 31 "gperf/csp13values.gperf"
    {"Admin", 0x02},
    {"",0},
#line 112 "gperf/csp13values.gperf"
    {"AC", 0x52},
#line 178 "gperf/csp13values.gperf"
    {"PRIORITYSTORE", 0x9B},
#line 149 "gperf/csp13values.gperf"
    {"Bold", 0x7D},
#line 152 "gperf/csp13values.gperf"
    {"Black", 0x80},
#line 107 "gperf/csp13values.gperf"
    {"WAPSMS", 0x4D},
#line 182 "gperf/csp13values.gperf"
    {"IR", 0x9F},
#line 105 "gperf/csp13values.gperf"
    {"USER_MOBILE_NUMBER", 0x4B},
#line 177 "gperf/csp13values.gperf"
    {"PRIORITYREJECT", 0x9A},
#line 69 "gperf/csp13values.gperf"
    {"text/plain", 0x28},
#line 194 "gperf/csp13values.gperf"
    {"DETECT", 0xAB},
#line 199 "gperf/csp13values.gperf"
    {"PP_CITY", 0xB0},
#line 49 "gperf/csp13values.gperf"
    {"Mod", 0x14},
#line 58 "gperf/csp13values.gperf"
    {"PrivilegeLevel", 0x1D},
    {"",0},
#line 108 "gperf/csp13values.gperf"
    {"WAPUDP", 0x4E},
#line 56 "gperf/csp13values.gperf"
    {"Private", 0x1B},
#line 120 "gperf/csp13values.gperf"
    {"ANU", 0x5A},
#line 74 "gperf/csp13values.gperf"
    {"Type", 0x2D},
#line 200 "gperf/csp13values.gperf"
    {"PP_COUNTRY", 0xB1},
    {"",0},
#line 122 "gperf/csp13values.gperf"
    {"ANXIOUS", 0x5C},
#line 89 "gperf/csp13values.gperf"
    {"ANC", 0x3C},
#line 174 "gperf/csp13values.gperf"
    {"USER_INTENTION", 0x97},
    {"",0}, {"",0},
#line 208 "gperf/csp13values.gperf"
    {"USER_AGE_MIN", 0xB9},
#line 212 "gperf/csp13values.gperf"
    {"CURRENT_SUBSCRIBER", 0xBD},
#line 170 "gperf/csp13values.gperf"
    {"USER_CITY", 0x93},
#line 135 "gperf/csp13values.gperf"
    {"INVINCIBLE", 0x6C},
#line 46 "gperf/csp13values.gperf"
    {"Inband", 0x11},
#line 171 "gperf/csp13values.gperf"
    {"USER_COUNTRY", 0x94},
#line 142 "gperf/csp13values.gperf"
    {"SAD", 0x73},
    {"",0}, {"",0},
#line 37 "gperf/csp13values.gperf"
    {"Closed", 0x08},
#line 47 "gperf/csp13values.gperf"
    {"IM", 0x12},
#line 141 "gperf/csp13values.gperf"
    {"PDA", 0x72},
#line 50 "gperf/csp13values.gperf"
    {"Name", 0x15},
    {"",0},
#line 156 "gperf/csp13values.gperf"
    {"Maroon", 0x84},
#line 123 "gperf/csp13values.gperf"
    {"ASHAMED", 0x5D},
    {"",0}, {"",0},
#line 210 "gperf/csp13values.gperf"
    {"MinimumAge", 0xBB},
#line 153 "gperf/csp13values.gperf"
    {"Silver", 0x81},
#line 103 "gperf/csp13values.gperf"
    {"USER_ID", 0x49},
#line 129 "gperf/csp13values.gperf"
    {"DISCREET", 0x64},
#line 167 "gperf/csp13values.gperf"
    {"Aqua", 0x8F},
    {"",0},
#line 150 "gperf/csp13values.gperf"
    {"Italic", 0x7E},
#line 134 "gperf/csp13values.gperf"
    {"IN_LOVE", 0x6B},
    {"",0}, {"",0},
#line 160 "gperf/csp13values.gperf"
    {"Green", 0x88},
#line 33 "gperf/csp13values.gperf"
    {"application/vnd.wap.mms-message", 0x04},
#line 185 "gperf/csp13values.gperf"
    {"IA", 0xA2},
    {"",0}, {"",0},
#line 63 "gperf/csp13values.gperf"
    {"Restricted", 0x22},
    {"",0},
#line 207 "gperf/csp13values.gperf"
    {"USER_AGE_MAX", 0xB8},
#line 147 "gperf/csp13values.gperf"
    {"Big", 0x7B},
    {"",0},
#line 92 "gperf/csp13values.gperf"
    {"GROUP_NAME", 0x3E},
#line 57 "gperf/csp13values.gperf"
    {"PrivateMessaging", 0x1C},
#line 117 "gperf/csp13values.gperf"
    {"GC", 0x57},
    {"",0},
#line 124 "gperf/csp13values.gperf"
    {"AVAILABLE", 0x5F},
    {"",0},
#line 84 "gperf/csp13values.gperf"
    {"ShowID", 0x37},
#line 131 "gperf/csp13values.gperf"
    {"EXCITED", 0x66},
#line 111 "gperf/csp13values.gperf"
    {"AND", 0x51},
    {"",0}, {"",0},
#line 173 "gperf/csp13values.gperf"
    {"USER_GENDER", 0x96},
#line 42 "gperf/csp13values.gperf"
    {"GR", 0x0D},
#line 196 "gperf/csp13values.gperf"
    {"OEU", 0xAD},
#line 53 "gperf/csp13values.gperf"
    {"Open", 0x18},
#line 121 "gperf/csp13values.gperf"
    {"ANGRY", 0x5B},
#line 93 "gperf/csp13values.gperf"
    {"GROUP_TOPIC", 0x3F},
#line 83 "gperf/csp13values.gperf"
    {"PENDING", 0x36},
#line 193 "gperf/csp13values.gperf"
    {"GMU", 0xAA},
#line 190 "gperf/csp13values.gperf"
    {"GMAU", 0xA7},
#line 68 "gperf/csp13values.gperf"
    {"text/", 0x27},
#line 70 "gperf/csp13values.gperf"
    {"text/x-vCalendar", 0x29},
#line 38 "gperf/csp13values.gperf"
    {"Default", 0x09},
#line 106 "gperf/csp13values.gperf"
    {"USER_ONLINE_STATUS", 0x4C},
#line 104 "gperf/csp13values.gperf"
    {"USER_LAST_NAME", 0x4A},
#line 162 "gperf/csp13values.gperf"
    {"Olive", 0x8A},
#line 163 "gperf/csp13values.gperf"
    {"Yellow", 0x8B},
#line 71 "gperf/csp13values.gperf"
    {"text/x-vCard", 0x2A},
#line 192 "gperf/csp13values.gperf"
    {"GMR", 0xA9},
#line 95 "gperf/csp13values.gperf"
    {"GROUP_USER_ID_OWNER", 0x41},
#line 140 "gperf/csp13values.gperf"
    {"OTHER", 0x71},
#line 144 "gperf/csp13values.gperf"
    {"www.openmobilealliance.org", 0x78},
    {"",0},
#line 169 "gperf/csp13values.gperf"
    {"CLC", 0x91},
#line 86 "gperf/csp13values.gperf"
    {"Tiny", 0x39},
    {"",0}, {"",0},
#line 54 "gperf/csp13values.gperf"
    {"Outband", 0x19},
#line 44 "gperf/csp13values.gperf"
    {"https://", 0x0F},
#line 115 "gperf/csp13values.gperf"
    {"CLCR", 0x55},
#line 130 "gperf/csp13values.gperf"
    {"EMAIL", 0x65},
    {"",0},
#line 79 "gperf/csp13values.gperf"
    {"GM", 0x32},
#line 113 "gperf/csp13values.gperf"
    {"BLC", 0x53},
#line 114 "gperf/csp13values.gperf"
    {"BLUC", 0x54},
#line 132 "gperf/csp13values.gperf"
    {"HAPPY", 0x67},
    {"",0},
#line 110 "gperf/csp13values.gperf"
    {"GROUP_USER_ID_AUTOJOIN", 0x50},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 138 "gperf/csp13values.gperf"
    {"MOBILE_PHONE", 0x6F},
#line 128 "gperf/csp13values.gperf"
    {"COMPUTER", 0x63},
#line 168 "gperf/csp13values.gperf"
    {"ATCL", 0x90},
#line 29 "gperf/csp13values.gperf"
    {"AccessType", 0x00},
#line 30 "gperf/csp13values.gperf"
    {"ActiveUsers", 0x01},
#line 159 "gperf/csp13values.gperf"
    {"Fuchsia", 0x87},
#line 127 "gperf/csp13values.gperf"
    {"CLI", 0x62},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 90 "gperf/csp13values.gperf"
    {"GROUP_ID", 0x3D},
#line 126 "gperf/csp13values.gperf"
    {"CALL", 0x61},
    {"",0},
#line 81 "gperf/csp13values.gperf"
    {"DENIED", 0x34},
#line 82 "gperf/csp13values.gperf"
    {"GRANTED", 0x35},
    {"",0},
#line 164 "gperf/csp13values.gperf"
    {"Navy", 0x8C},
    {"",0}, {"",0},
#line 32 "gperf/csp13values.gperf"
    {"application/", 0x03},
    {"",0},
#line 161 "gperf/csp13values.gperf"
    {"Lime", 0x89},
#line 94 "gperf/csp13values.gperf"
    {"GROUP_USER_ID_JOINED", 0x40},
#line 41 "gperf/csp13values.gperf"
    {"G", 0x0C},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 209 "gperf/csp13values.gperf"
    {"EG", 0xBA},
#line 116 "gperf/csp13values.gperf"
    {"CLD", 0x56},
#line 154 "gperf/csp13values.gperf"
    {"Gray", 0x82},
    {"",0},
#line 189 "gperf/csp13values.gperf"
    {"DoNotNotify", 0xA6},
#line 61 "gperf/csp13values.gperf"
    {"Request", 0x20},
    {"",0},
#line 77 "gperf/csp13values.gperf"
    {"www.wireless-village.org", 0x30},
    {"",0},
#line 39 "gperf/csp13values.gperf"
    {"DisplayName", 0x0A},
#line 213 "gperf/csp13values.gperf"
    {"FORMER_SUBSCRIBER", 0xBE},
#line 139 "gperf/csp13values.gperf"
    {"NOT_AVAILABLE", 0x70},
    {"",0},
#line 125 "gperf/csp13values.gperf"
    {"BORED", 0x60},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 118 "gperf/csp13values.gperf"
    {"GD", 0x58},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 34 "gperf/csp13values.gperf"
    {"application/x-sms", 0x05},
#line 191 "gperf/csp13values.gperf"
    {"GMG", 0xA8},
    {"",0}, {"",0}, {"",0},
#line 91 "gperf/csp13values.gperf"
    {"History", 0x3D},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 119 "gperf/csp13values.gperf"
    {"GLC", 0x59},
#line 184 "gperf/csp13values.gperf"
    {"GLUC", 0xA1},
    {"",0},
#line 45 "gperf/csp13values.gperf"
    {"image/", 0x10},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 43 "gperf/csp13values.gperf"
    {"http://", 0x0E},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0},
#line 143 "gperf/csp13values.gperf"
    {"SLEEPY", 0x74},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0},
#line 80 "gperf/csp13values.gperf"
    {"Validity", 0x33},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
    {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0}, {"",0},
#line 48 "gperf/csp13values.gperf"
    {"MaxActiveUsers", 0x13},
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
    {"",0},
#line 195 "gperf/csp13values.gperf"
    {"FORKALL", 0xAC}
  };

#ifdef __GNUC__
__inline
#endif
const struct TokenEntry *
findToken (str, len)
     register const char *str;
     register unsigned int len;
{
  if (len <= MAX_WORD_LENGTH && len >= MIN_WORD_LENGTH)
    {
      register int key = hash (str, len);

      if (key <= MAX_HASH_VALUE && key >= 0)
        {
          register const char *s = wordlist[key].name;

          if (*str == *s && !strncmp (str + 1, s + 1, len - 1) && s[len] == '\0')
            return &wordlist[key];
        }
    }
  return 0;
}
#line 216 "gperf/csp13values.gperf"


int csp13ValueTokenToKey(const char * str, int len)
{
    const struct TokenEntry * tag = findToken(str, len);
    return tag ? tag->key : -1;
}

