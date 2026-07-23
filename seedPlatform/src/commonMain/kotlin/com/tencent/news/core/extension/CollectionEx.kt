package com.tencent.news.core.extension


fun <T> List<T>?.safeGet(index: Int): T? {
    this ?: return null
    return if (isValidIndex(index)) {
        get(index)
    } else {
        null
    }
}

fun <T> MutableList<T>?.safeRemoveAt(index: Int): T? {
    this ?: return null
    return if (isValidIndex(index)) {
        removeAt(index)
    } else {
        null
    }
}

inline fun Collection<*>?.safeSize(): Int {
    return this?.size ?: 0
}

fun Collection<*>.isValidIndex(index: Int): Boolean {
    return index in indices
}

fun Collection<*>.isValidInsertIndex(index: Int): Boolean {
    return index in 0..size
}

fun Collection<*>?.isNotNullOrEmpty(): Boolean {
    return !isNullOrEmpty()
}

inline fun <T> List<T>?.takeIfNotEmpty(): List<T>? {
    return this.takeIf { !it.isNullOrEmpty() }
}

inline fun <T> Collection<T>.safeForEach(action: (T) -> Unit) {
    val safeIterator = ArrayList(this) // 防止ConcurrentModify
    safeIterator.forEach(action)
}

// 如果list当前就是安全的，返回自己；否则造一个安全的
fun <T> List<T?>?.getOrChangeSafeList(): List<T> {
    this ?: return listOf()
    if (this.any { it == null }) {
        return safeList(this)
    }
    return this as List<T>
}

fun <T> safeList(target: Collection<T?>?): List<T> {
    target ?: return listOf()
    // 优先构造 ArrayList 再filter，防止 ConcurrentModify
    return ArrayList(target).filterNotNull()
}

fun <T> safeList(vararg target: T?): List<T> {
    return ArrayList(target.filterNotNull())
}

fun <T> safeArrayToList(target: Array<T?>?): List<T> {
    target ?: return listOf()
    return ArrayList(target.filterNotNull())
}

fun <T> safeList(vararg targets: Collection<T?>?): List<T> {
    val result = ArrayList<T>()
    targets.forEach { list ->
        result.safeAddAll(list)
    }
    return result
}

fun <T> List<T>?.safeSubList(
    fromIndex: Int,
    toIndex: Int = this?.size ?: 0,
    nullIfInvalid: Boolean = false,
): List<T>? {
    val result = mutableListOf<T>()
    val invalidResult = if (nullIfInvalid) null else result
    val list = this
        ?: return invalidResult

    if (fromIndex < 0) {
        return invalidResult
    }
    if (fromIndex > toIndex) {
        return invalidResult
    }
    if (fromIndex >= list.size) {
        return invalidResult
    }
    if (toIndex > list.size) {
        return list.subList(fromIndex, list.size)
    }
    return list.subList(fromIndex, toIndex)
}

fun <T> MutableCollection<T>?.clearAndAddAll(data: Collection<T?>?) {
    this ?: return

    clear()
    safeAddAll(data)
}

fun <T> MutableCollection<T>?.safeAdd(data: T?) {
    data ?: return
    this?.add(data)
}

fun <T> MutableList<T>?.safeAdd(
    index: Int,
    data: T?,
    appendWhenOverflow: Boolean = true
) {
    this ?: return
    data ?: return
    if (index < 0) return

    if (index > size) {
        if (appendWhenOverflow) {
            add(size, data)
        }
    } else {
        add(index, data)
    }
}

fun <T> MutableCollection<T>?.addIfNotExist(data: T?, predict: (T) -> Boolean) {
    this ?: return
    data ?: return

    if (!any(predict)) {
        add(data)
    }
}

fun <T> MutableCollection<T>?.addIfAbsent(data: T?) {
    this ?: return
    data ?: return

    val exist = any { it == data }
    if (!exist) {
        add(data)
    }
}

fun <T> MutableCollection<T>?.removeIfExist(predict: (T) -> Boolean) {
    val target = this?.firstOrNull { predict(it) } ?: return
    remove(target)
}

/**
 * 安全删除指定元素（引用相等匹配）
 * 避免某些 Android 版本 ArrayList.remove(Object) 在元素不存在时，
 * indexOf 返回 -1 后直接调用 remove(-1) 导致 ArrayIndexOutOfBoundsException
 */
fun <T> MutableCollection<T>?.safeRemove(element: T?) {
    element ?: return
    val iterator = this?.iterator() ?: return
    while (iterator.hasNext()) {
        if (iterator.next() === element) {
            iterator.remove()
            break
        }
    }
}

fun <T> MutableCollection<T>?.safeAddAll(data: Collection<T?>?) {
    data ?: return
    this?.addAll(data.filterNotNull())
}

fun <T> MutableList<T>?.safeAddAll(index: Int, data: Collection<T?>?) {
    data ?: return
    this?.addAll(index, data.filterNotNull())
}

fun <T> MutableList<T>?.safeReplace(old: T, new: T): Boolean {
    this ?: return false

    forEachIndexed { index, data ->
        if (data == old) {
            this[index] = new
            return true
        }
    }
    return false
}

fun <T> MutableList<T>?.safeReplaceList(old: T, newList: List<T>): Boolean {
    this ?: return false

    val index = indexOf(old)
    if (index < 0) {
        return false
    }

    remove(old)
    safeAddAll(index, newList)
    return true
}

fun Array<String?>?.isEmptyString(): Boolean {
    if (this.isNullOrEmpty()) {
        return true
    }
    return !any { it.isNotNullOrBlank() }
}

fun <T> List<T>?.append(vararg value: T): List<T> {
    val result = this?.toMutableList() ?: mutableListOf()
    result.addAll(value.asList())
    return result
}