package com.tencent.news.core.vm


// 业务解耦用的占坑接口，业务侧实现减：ModelToVM.kt

// 【qnMedia】
interface IAudioFeedsVMHolderStub

// 【qnDetail】
interface IDetailModelStub

// 【qnUser】
interface IAiShareMetadataRepoStub
interface IUserHeaderVMHolderStub

// 【qnAd】
interface IAdVMItemStub     // 广告item vm（后续解耦彻底后，只留这一个）

// 【IFeedsVMItem 所属】
interface IFeedsVMItemStub  // 列表item vm（后续解耦彻底后，只留这一个）
interface IGameItemVMStub   // 游戏信息vm（IGameInfo依赖此stub）