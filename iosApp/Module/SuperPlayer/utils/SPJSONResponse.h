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

/**
 *  json解析处理类
 */
@interface SPJSONResponse : NSObject {
}

@property (nonatomic, retain, readonly) id rootObject;
@property (nonatomic, retain, readonly) id responseData;
@property (nonatomic, assign) BOOL rootIsArray;  // by zephyrzhou，看比赛的接口中，有返回array的
/**
 *  过滤非法字符
 *
 *  @param json json字符串
 *  @return 过滤后的json字符串
 */
+ (NSString *)filterJSON:(NSString *)json;

/**
 *  解析json字符串
 *
 *  @param json json字符串
 *  @return 解析出的字典
 */
+ (id)parseJSON:(NSString *)json;

/**
 * 解析nsdata，解析结果保存到rootObject
 *
 * @param data 待解析的data
 * @return 解析过程中的错误，无错误则返回nil
 */
- (NSError *)processResponseData:(NSData *)data;

/**
 * 解析字符串，解析结果保存到rootObject
 *
 * @param string 待解析字符串
 * @return 解析过程中的错误，无错误则返回nil
 */
- (NSError *)processString:(NSString *)string;
@end
