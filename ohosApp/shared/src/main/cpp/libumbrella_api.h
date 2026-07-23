#ifndef KONAN_LIBUMBRELLA_H
#define KONAN_LIBUMBRELLA_H
#ifdef __cplusplus
extern "C" {
#endif
#ifdef __cplusplus
typedef bool            libumbrella_KBoolean;
#else
typedef _Bool           libumbrella_KBoolean;
#endif
typedef unsigned short     libumbrella_KChar;
typedef signed char        libumbrella_KByte;
typedef short              libumbrella_KShort;
typedef int                libumbrella_KInt;
typedef long long          libumbrella_KLong;
typedef unsigned char      libumbrella_KUByte;
typedef unsigned short     libumbrella_KUShort;
typedef unsigned int       libumbrella_KUInt;
typedef unsigned long long libumbrella_KULong;
typedef float              libumbrella_KFloat;
typedef double             libumbrella_KDouble;
typedef float __attribute__ ((__vector_size__ (16))) libumbrella_KVector128;
typedef void*              libumbrella_KNativePtr;
struct libumbrella_KType;
typedef struct libumbrella_KType libumbrella_KType;

typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Byte;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Short;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Int;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Long;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Float;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Double;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Char;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Boolean;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Unit;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_UByte;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_UShort;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_UInt;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_ULong;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_com_tencent_kmm_ohos_DemoOhosStartupConfig;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_com_tencent_kmm_startup_std_config_BasicAppStartupConfig;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Array;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_reflect_KClass;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_kotlin_Any;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_com_tencent_tmm_knoi_type_JSValue;
typedef struct {
  libumbrella_KNativePtr pinned;
} libumbrella_kref_TriggerLinkTaskPlaceHolder;

extern void com_tencent_tmm_knoi_initEnv(void* env, void* value, libumbrella_KBoolean debug);
extern void com_tencent_tmm_knoi_initBridge();

typedef struct {
  /* Service functions. */
  void (*DisposeStablePointer)(libumbrella_KNativePtr ptr);
  void (*DisposeString)(const char* string);
  libumbrella_KBoolean (*IsInstance)(libumbrella_KNativePtr ref, const libumbrella_KType* type);
  libumbrella_kref_kotlin_Byte (*createNullableByte)(libumbrella_KByte);
  libumbrella_KByte (*getNonNullValueOfByte)(libumbrella_kref_kotlin_Byte);
  libumbrella_kref_kotlin_Short (*createNullableShort)(libumbrella_KShort);
  libumbrella_KShort (*getNonNullValueOfShort)(libumbrella_kref_kotlin_Short);
  libumbrella_kref_kotlin_Int (*createNullableInt)(libumbrella_KInt);
  libumbrella_KInt (*getNonNullValueOfInt)(libumbrella_kref_kotlin_Int);
  libumbrella_kref_kotlin_Long (*createNullableLong)(libumbrella_KLong);
  libumbrella_KLong (*getNonNullValueOfLong)(libumbrella_kref_kotlin_Long);
  libumbrella_kref_kotlin_Float (*createNullableFloat)(libumbrella_KFloat);
  libumbrella_KFloat (*getNonNullValueOfFloat)(libumbrella_kref_kotlin_Float);
  libumbrella_kref_kotlin_Double (*createNullableDouble)(libumbrella_KDouble);
  libumbrella_KDouble (*getNonNullValueOfDouble)(libumbrella_kref_kotlin_Double);
  libumbrella_kref_kotlin_Char (*createNullableChar)(libumbrella_KChar);
  libumbrella_KChar (*getNonNullValueOfChar)(libumbrella_kref_kotlin_Char);
  libumbrella_kref_kotlin_Boolean (*createNullableBoolean)(libumbrella_KBoolean);
  libumbrella_KBoolean (*getNonNullValueOfBoolean)(libumbrella_kref_kotlin_Boolean);
  libumbrella_kref_kotlin_Unit (*createNullableUnit)(void);
  libumbrella_kref_kotlin_UByte (*createNullableUByte)(libumbrella_KUByte);
  libumbrella_KUByte (*getNonNullValueOfUByte)(libumbrella_kref_kotlin_UByte);
  libumbrella_kref_kotlin_UShort (*createNullableUShort)(libumbrella_KUShort);
  libumbrella_KUShort (*getNonNullValueOfUShort)(libumbrella_kref_kotlin_UShort);
  libumbrella_kref_kotlin_UInt (*createNullableUInt)(libumbrella_KUInt);
  libumbrella_KUInt (*getNonNullValueOfUInt)(libumbrella_kref_kotlin_UInt);
  libumbrella_kref_kotlin_ULong (*createNullableULong)(libumbrella_KULong);
  libumbrella_KULong (*getNonNullValueOfULong)(libumbrella_kref_kotlin_ULong);

  /* User functions. */
  struct {
    struct {
      struct {
        struct {
          struct {
            struct {
              struct {
                libumbrella_KType* (*_type)(void);
                libumbrella_kref_com_tencent_kmm_ohos_DemoOhosStartupConfig (*_instance)();
                const char* (*get_APP_ID)(libumbrella_kref_com_tencent_kmm_ohos_DemoOhosStartupConfig thiz);
                const char* (*get_PACKAGE_NAME)(libumbrella_kref_com_tencent_kmm_ohos_DemoOhosStartupConfig thiz);
                const char* (*get_QIMEI_APP_KEY)(libumbrella_kref_com_tencent_kmm_ohos_DemoOhosStartupConfig thiz);
                libumbrella_kref_com_tencent_kmm_startup_std_config_BasicAppStartupConfig (*build)(libumbrella_kref_com_tencent_kmm_ohos_DemoOhosStartupConfig thiz, libumbrella_KBoolean isDebug, const char* appVersion);
              } DemoOhosStartupConfig;
              struct {
                struct {
                  libumbrella_KType* (*_type)(void);
                  libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider (*HarmonyStartupProviderProvider)();
                  libumbrella_KInt (*getMinParamsSize)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider thiz, const char* method);
                  libumbrella_kref_kotlin_Array (*getParamsTypeList)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider thiz, const char* method);
                  libumbrella_kref_kotlin_reflect_KClass (*getReturnType)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider thiz, const char* method);
                  libumbrella_kref_kotlin_Any (*invoke)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider thiz, const char* method, libumbrella_kref_kotlin_Array params);
                } HarmonyStartupProviderProvider;
                struct {
                  libumbrella_KType* (*_type)(void);
                  libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider (*HarmonyStartupProvider)();
                  void (*onAppBackground)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz);
                  void (*onAppForeground)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz);
                  void (*onAppStartup)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_KBoolean isDebug, const char* appVersion, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue nativeContext);
                  void (*setupAppAlert)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue alert);
                  void (*setupAppConfig)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue appConfig);
                  void (*setupAppGyroscope)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue gyroscope);
                  void (*setupAppLocation)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue location);
                  void (*setupAppPermission)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue permission);
                  void (*setupAppReport)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue appReport);
                  void (*setupAppResManager)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue resManager);
                  void (*setupAppRouter)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue router);
                  void (*setupAppShare)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue appShare);
                  void (*setupAppStatus)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue status);
                  void (*setupAppStatusBar)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue statusBar);
                  void (*setupAppWindow)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue bridge);
                  void (*setupSystemVolumeController)(libumbrella_kref_com_tencent_kmm_ohos_startup_HarmonyStartupProvider thiz, libumbrella_kref_com_tencent_tmm_knoi_type_JSValue volumeController);
                } HarmonyStartupProvider;
                libumbrella_KInt (*com_tencent_kmm_ohos_startup_HarmonyStartupProvider$stableprop_getter)();
                libumbrella_KInt (*com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider$stableprop_getter)();
                libumbrella_KInt (*com_tencent_kmm_ohos_startup_HarmonyStartupProvider$stableprop_getter_)();
                libumbrella_KInt (*com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider$stableprop_getter_)();
                libumbrella_KInt (*com_tencent_kmm_ohos_startup_HarmonyStartupProvider$stableprop_getter__)();
                libumbrella_KInt (*com_tencent_kmm_ohos_startup_HarmonyStartupProviderProvider$stableprop_getter__)();
                void (*setupOhosStandardStartupBridges)();
              } startup;
              libumbrella_KInt (*com_tencent_kmm_ohos_DemoOhosStartupConfig$stableprop_getter)();
            } ohos;
          } kmm;
          struct {
            struct {
              struct {
                struct {
                  void (*registerHarmonyStartupProviderProvider)();
                } umbrella;
                void (*initUmbrella)();
              } modules;
              void (*initBridge)();
              void (*initEnvExport)(void* env, void* value, libumbrella_KBoolean debug);
              void (*initialize)();
              void (*preInitEnv)(void* env, libumbrella_KBoolean debug);
            } knoi;
          } tmm;
        } tencent;
      } com;
      struct {
        libumbrella_KType* (*_type)(void);
        libumbrella_kref_TriggerLinkTaskPlaceHolder (*TriggerLinkTaskPlaceHolder)();
      } TriggerLinkTaskPlaceHolder;
      void* (*get_ktRenderCallNativeCallback)();
      void (*set_ktRenderCallNativeCallback)(void* set);
      libumbrella_KInt (*TriggerLinkTaskPlaceHolder$stableprop_getter)();
      void (*callKotlinMethod)(libumbrella_KInt methodId, void* arg0, void* arg1, void* arg2, void* arg3, void* arg4, void* arg5);
      libumbrella_KInt (*initKuikly)();
      libumbrella_KInt (*TriggerLinkTaskPlaceHolder$stableprop_getter_)();
    } root;
  } kotlin;
} libumbrella_ExportedSymbols;
extern libumbrella_ExportedSymbols* libumbrella_symbols(void);
#ifdef __cplusplus
}  /* extern "C" */
#endif
#endif  /* KONAN_LIBUMBRELLA_H */
