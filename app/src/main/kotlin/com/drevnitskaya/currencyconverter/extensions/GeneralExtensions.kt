package com.drevnitskaya.currencyconverter.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * The smartest [Gson.fromJson] of all. It automatically figure out the type, so you don't need to pass in the type anymore.
 *
 * This is also the last time you see the [TypeToken], it works with Generics.
 * ```
 * val p1: Person = Gson().fromJson(jsonString)
 * val p2 = Gson().fromJson<Person>(anotherJsonString)
 * ```
 */
inline fun <reified T> Gson.fromJson(json: String): T = fromJson(json, object : TypeToken<T>() {}.type)

/**
 * Add [toJson] to everything.
 * ```
 * val p1 = Person()
 * val json = p1.toJson()
 * ```
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T> T.toJson(gson: Gson = Gson()): String = gson.toJson(this)

/**
 * Add [parse] to String
 * ```
 * val json = ...
 * val p1: Person = json.parse()
 * ```
 */
inline fun <reified T> String.parse(gson: Gson = Gson()): T = gson.fromJson(this)