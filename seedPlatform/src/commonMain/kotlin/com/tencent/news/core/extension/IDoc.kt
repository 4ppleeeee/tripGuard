package com.tencent.news.core.extension


// todo 架构说明: 标记用接口，用于跟踪全局接口定义； 在AS中 ctrl + H 快捷键查看继承关系
// 后续可以考虑用这个继承关系自动生成一些文档
interface IDoc


// 📚 todo doc: 暴露给宿主的业务接口集合
interface IServiceDoc : IDoc


// 📚 todo doc: 对鸿蒙暴露的接口
interface IOhosExportDoc : IDoc


// 📚 todo doc: 数据结构相关
interface ICmsModelDoc : IDoc           // cms发文的介质（文章、事件、tag、评论 等等）
interface ICmsModelDtoItemDoc : IDoc
interface IDtoDoc : IDoc
interface IItemDtoDoc : IDtoDoc         // 列表文章item的所有dto
interface IAdOrderDtoDoc : IDtoDoc      // 广告订单order的所有dto
interface IStructWidgetDoc : IDtoDoc    // 各种结构化组件


// 📚 todo doc: 生命周期相关
interface ILifecycleDoc : IDoc
interface ICellLifecycleDoc : ILifecycleDoc     // cell生命周期（类比：ViewHolder）
interface IListLifecycleDoc : ILifecycleDoc     // 列表生命周期（类比：RecyclerView）
interface IPageLifecycleDoc : ILifecycleDoc     // 页面生命周期（类比：Activity/Fragment）


// 用户行为相关（点赞、收藏、关注 等）
interface IUgcDoc : IDoc


// 📚 todo doc: vm
interface IVMDoc : IDoc


// 📚 todo doc: UI组件
interface IUIDoc : IDoc
interface IBtnViewDoc : IUIDoc      // 按钮view


// 📚 todo doc: 配置（主要是shiply）
interface IConfigDoc : IDoc


// 📚 todo doc: 枚举值
interface IEnumDoc : IDoc
interface IListEnumDoc : IEnumDoc   // 列表相关枚举
interface IAdEnumDoc : IEnumDoc     // 广告相关
interface IGameEnumDoc : IEnumDoc   // 游戏联运相关


// 📚 todo doc: 日志
interface ILogDoc : IDoc


// 📚 todo doc: 上报
interface IReportDoc : IDoc


// 📚 todo doc: 模块初始化（普遍是做依赖注入绑定）
interface ISetUpDoc : IDoc


// 📚 todo doc: 各类组件注册
interface IRegistryDoc : IDoc
interface IStructWidgetRegistryDoc : IRegistryDoc // 结构化页面（品字形）组件注册