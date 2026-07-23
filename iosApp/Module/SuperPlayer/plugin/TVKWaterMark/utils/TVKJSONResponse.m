//
// Copyright 2009-2010 Facebook
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

#import "TVKJSONResponse.h"
#import <UIKit/UIDevice.h>
#import "SPVcSystemInfo.h"

static NSString* const gExtJSONErrorDomain = @"three20.ext.json";
static NSInteger const gExtJSONErrorCodeInvalidJSON = 100;
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
@implementation TVKJSONResponse

+ (NSString*)filterJSON:(NSString*)json {
    if (json == nil || [json length] == 0) {
        return nil;
    }
    const char* cStringJSON = [json cStringUsingEncoding:NSUTF8StringEncoding];
    if (cStringJSON) {
        int len = (int)strlen(cStringJSON);
        char* stringJSON = malloc(len + 1);
        if (stringJSON) {
            strncpy(stringJSON, cStringJSON, len);
            stringJSON[len] = '\0';
            char* p = stringJSON;
            for (int i = 0; i < len; i++) {
                /*
                for (char c = 0x01; c<=0x1f; c++) {
                    if (*p == c){
                        *p = ' ';
                        break;
                    }
                }*/
                if (*p <= 0x1f && *p >= 0x01) {
                    *p = ' ';
                }
                p++;
            }
            json = [NSString stringWithCString:stringJSON encoding:NSUTF8StringEncoding];
            free(stringJSON);
            stringJSON = 0;
        } else {
            for (char c = 0x01; c <= 0x1f; c++) {
                json = [json stringByReplacingOccurrencesOfString:[NSString stringWithFormat:@"%c", c] withString:@" "];
            }
        }
    }

    return json;
}

+ (id)parseJSON:(NSString*)json {
    if (json == nil) {
        return nil;
    }

    static NSNumber* s_isSupportNSJSONSerializationNum = nil;
    if (s_isSupportNSJSONSerializationNum == nil) {
        if (SYSTEM_VERSION_LESS_THAN(@"5.0")) {
            s_isSupportNSJSONSerializationNum = [[NSNumber alloc] initWithBool:NO];
        } else {
            s_isSupportNSJSONSerializationNum = [[NSNumber alloc] initWithBool:YES];
        }
    }

    if ([s_isSupportNSJSONSerializationNum boolValue]) {
        @try {
            NSError* error = nil;
            id ret = [NSJSONSerialization JSONObjectWithData:[json dataUsingEncoding:NSUTF8StringEncoding]
                                                     options:NSJSONReadingMutableLeaves
                                                       error:&error];
            return ret;
        } @catch (NSException* exception) {
        } @finally {
        }
    }

    return nil;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
- (NSError*)processResponseData:(NSData *)data {
    // This response is designed for NSData objects, so if we get anything else it's probably a mistake
    //  TTDASSERT([data isKindOfClass:[NSData class]]);

    if ([data isKindOfClass:[NSData class]]) {
        NSString* json = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        _responseData = data;
        return [self processString:json];
    } else {
        return [NSError errorWithDomain:gExtJSONErrorDomain code:gExtJSONErrorCodeInvalidJSON userInfo:nil];
    }
}

- (NSError*)processString:(NSString*)string {
    // This response is designed for NSData objects, so if we get anything else it's probably a mistake
    //  TTDASSERT([data isKindOfClass:[NSData class]]);

    if ([string isKindOfClass:[NSString class]]) {
        NSString* json = string;

        NSRange beginrange = [json rangeOfString:@"{"];
        NSRange endrange = [json rangeOfString:@"}" options:NSBackwardsSearch];
        if (self.rootIsArray) {
            beginrange = [json rangeOfString:@"["];
            endrange = [json rangeOfString:@"]" options:NSBackwardsSearch];
        }

        if (beginrange.location != NSNotFound && endrange.location != NSNotFound) {
            json = [json substringToIndex:endrange.location + 1];
            json = [json substringFromIndex:beginrange.location];
            json = [TVKJSONResponse filterJSON:json];

            _rootObject = [TVKJSONResponse parseJSON:json];
        } else if ([json hasSuffix:@"=null;"]) {
            // 返回数据为空
            _rootObject = [[NSDictionary alloc] init];
        } else {
            // 错误的json格式
            _rootObject = nil;
        }
    }

    NSError* err = nil;
    if (!_rootObject) {
        err = [NSError errorWithDomain:gExtJSONErrorDomain code:gExtJSONErrorCodeInvalidJSON userInfo:nil];
    };

    return err;
}

@end
