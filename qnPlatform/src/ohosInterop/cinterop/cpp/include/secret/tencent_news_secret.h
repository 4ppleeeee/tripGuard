#ifndef TENCENT_NEWS_SECRET_H
#define TENCENT_NEWS_SECRET_H

#ifdef __cplusplus
extern "C" {
#endif

const char* tns_ad_des_key();
const char* tns_ad_des_iv();

const char* tns_mob_des_key();
const char* tns_mob_des_iv();

const char* tns_http_sign_secret();
const char* tns_http_sign_url_secret();

#ifdef __cplusplus
}
#endif

#endif
