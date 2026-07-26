package com.github.gezimos.inkos.helper

import android.util.Log
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.zip.GZIPInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WeatherData(
    val city: String = "",
    val temp: Double = 0.0,
    val unit: String = "C", // "C" or "F"
    val weatherCode: Int = 0,
    val description: String = "",
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    fun getDisplayTemp(): String {
        val convertedTemp = if (unit == "F") (temp * 9 / 5) + 32 else temp
        val rounded = Math.round(convertedTemp)
        return "$rounded°$unit"
    }

    fun getWeatherIcon(): String {
        if (description.isNotBlank()) {
            return when {
                description.contains("晴") -> "☀️"
                description.contains("多云") -> "⛅"
                description.contains("阴") -> "☁️"
                description.contains("雾") || description.contains("霾") || description.contains("沙") -> "🌫️"
                description.contains("雷") -> "🌩️"
                description.contains("暴雨") || description.contains("大雨") -> "🌧️"
                description.contains("雨") -> "🌧️"
                description.contains("雪") -> "❄️"
                else -> "🌤️"
            }
        }
        return when (weatherCode) {
            0 -> "☀️"
            1 -> "🌤️"
            2 -> "⛅"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55, 56, 57 -> "🌧️"
            61, 63, 65, 66, 67 -> "🌧️"
            71, 73, 75, 77 -> "❄️"
            80, 81, 82 -> "🌦️"
            85, 86 -> "🌨️"
            95, 96, 99 -> "🌩️"
            else -> "🌤️"
        }
    }

    fun getFormattedString(): String {
        if (!isSuccess) return if (errorMessage != null) "天气: $errorMessage" else "点击刷新天气"
        val icon = getWeatherIcon()
        val loc = if (city.isNotBlank()) "📍 $city · " else ""
        val humStr = if (humidity > 0) " 湿度 $humidity%" else ""
        return "$loc$icon $description ${getDisplayTemp()}$humStr"
    }
}

object WeatherRepository {
    private const val TAG = "WeatherRepository"

    suspend fun fetchWeather(customCity: String = "", unit: String = "C"): WeatherData = withContext(Dispatchers.IO) {
        try {
            // Priority 1: China Domestic API (中国天气网 CDN + 太平洋 IP 定位)
            val domesticResult = fetchByDomesticChina(customCity, unit)
            if (domesticResult.isSuccess) {
                Log.d(TAG, "Successfully fetched weather using China domestic API: ${domesticResult.city}")
                return@withContext domesticResult
            }

            // Priority 2: OpenSpeech China API (科大讯飞气象源)
            val openSpeechResult = fetchByOpenSpeechChina(customCity, unit)
            if (openSpeechResult.isSuccess) {
                Log.d(TAG, "Successfully fetched weather using OpenSpeech API: ${openSpeechResult.city}")
                return@withContext openSpeechResult
            }

            // Priority 3: International IP Geo (ip-api + open-meteo)
            val ipGeoResult = fetchByIpGeo(customCity, unit)
            if (ipGeoResult.isSuccess) return@withContext ipGeoResult

            // Priority 4: wttr.in Fallback
            val wttrResult = fetchFromWttrIn(customCity, unit)
            if (wttrResult.isSuccess) return@withContext wttrResult

            return@withContext WeatherData(isSuccess = false, errorMessage = "国内与海外天气源请求均超时，请检查网络")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather", e)
            return@withContext WeatherData(isSuccess = false, errorMessage = e.localizedMessage ?: "网络异常")
        }
    }

    /**
     * 国内优先数据源：中国天气网 etouch 接口 + 太平洋电脑网 IP 定位
     */
    private fun fetchByDomesticChina(customCity: String, unit: String): WeatherData {
        try {
            var targetCity = customCity.trim()

            // 如果没有自定义城市，通过国内 IP 接口 (PConline) 获取物理城市
            if (targetCity.isBlank()) {
                val ipUrl = URL("http://whois.pconline.com.cn/ipJson.jsp?json=true")
                val ipConn = (ipUrl.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                }

                if (ipConn.responseCode == 200) {
                    val stream = getUncompressedStream(ipConn)
                    val ipResponseBody = stream.bufferedReader(charset("GBK")).use { it.readText() }
                    val ipJson = JSONObject(ipResponseBody)
                    val city = ipJson.optString("city").ifBlank { ipJson.optString("pro") }
                    targetCity = sanitizeChineseCity(city)
                }
            }

            if (targetCity.isBlank()) {
                targetCity = "北京"
            }

            val sanitizedQuery = sanitizeChineseCity(targetCity)
            val encodedCity = URLEncoder.encode(sanitizedQuery, "UTF-8")
            val weatherUrl = URL("http://wthrcdn.etouch.cn/weather_mini?city=$encodedCity")
            val weatherConn = (weatherUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                requestMethod = "GET"
                setRequestProperty("Accept-Encoding", "gzip, deflate")
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }

            if (weatherConn.responseCode == 200) {
                val stream = getUncompressedStream(weatherConn)
                val body = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val json = JSONObject(body)
                if (json.optInt("status") == 1000) {
                    val data = json.getJSONObject("data")
                    val city = data.optString("city", targetCity)
                    val temp = data.optString("wendu", "0").toDoubleOrNull() ?: 0.0
                    val forecastArray = data.optJSONArray("forecast")
                    var desc = "晴"
                    if (forecastArray != null && forecastArray.length() > 0) {
                        desc = forecastArray.getJSONObject(0).optString("type", "晴")
                    }

                    return WeatherData(
                        city = city,
                        temp = temp,
                        unit = unit,
                        description = desc,
                        timestamp = System.currentTimeMillis(),
                        isSuccess = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchByDomesticChina failed", e)
        }
        return WeatherData(isSuccess = false)
    }

    /**
     * 国内备用数据源：科大讯飞/OpenSpeech 气象服务
     */
    private fun fetchByOpenSpeechChina(customCity: String, unit: String): WeatherData {
        try {
            var targetCity = customCity.trim()
            if (targetCity.isBlank()) targetCity = "北京"
            val sanitized = sanitizeChineseCity(targetCity)
            val encoded = URLEncoder.encode(sanitized, "UTF-8")
            val url = URL("http://autodev.openspeech.cn/csp/api/v2.1/weather?clientType=android&city=$encoded")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }

            if (conn.responseCode == 200) {
                val stream = getUncompressedStream(conn)
                val body = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val json = JSONObject(body)
                if (json.optInt("code") == 0) {
                    val data = json.getJSONObject("data")
                    val list = data.optJSONArray("list")
                    if (list != null && list.length() > 0) {
                        val today = list.getJSONObject(0)
                        val city = today.optString("city", targetCity)
                        val temp = today.optDouble("temp", 0.0)
                        val desc = today.optString("weather", "多云")
                        val humidity = today.optString("humidity", "0").replace("%", "").toIntOrNull() ?: 0

                        return WeatherData(
                            city = city,
                            temp = temp,
                            unit = unit,
                            description = desc,
                            humidity = humidity,
                            timestamp = System.currentTimeMillis(),
                            isSuccess = true
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchByOpenSpeechChina failed", e)
        }
        return WeatherData(isSuccess = false)
    }

    private fun fetchByIpGeo(customCity: String, unit: String): WeatherData {
        try {
            val ipUrl = URL("http://ip-api.com/json/?lang=zh-CN")
            val ipConn = (ipUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                requestMethod = "GET"
            }

            if (ipConn.responseCode == 200) {
                val ipResponseBody = getUncompressedStream(ipConn).bufferedReader().use { it.readText() }
                val ipJson = JSONObject(ipResponseBody)
                if (ipJson.optString("status") == "success") {
                    val lat = ipJson.optDouble("lat")
                    val lon = ipJson.optDouble("lon")
                    val city = if (customCity.isNotBlank()) customCity else ipJson.optString("city").ifBlank { ipJson.optString("regionName") }

                    val weatherUrl = URL("https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=auto")
                    val weatherConn = (weatherUrl.openConnection() as HttpURLConnection).apply {
                        connectTimeout = 3500
                        readTimeout = 3500
                        requestMethod = "GET"
                    }

                    if (weatherConn.responseCode == 200) {
                        val weatherBody = getUncompressedStream(weatherConn).bufferedReader().use { it.readText() }
                        val weatherJson = JSONObject(weatherBody)
                        val current = weatherJson.getJSONObject("current")

                        val temp = current.getDouble("temperature_2m")
                        val code = current.getInt("weather_code")
                        val humidity = current.optInt("relative_humidity_2m", 0)

                        return WeatherData(
                            city = city,
                            temp = temp,
                            unit = unit,
                            weatherCode = code,
                            description = getWeatherDescription(code),
                            humidity = humidity,
                            timestamp = System.currentTimeMillis(),
                            isSuccess = true
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchByIpGeo failed", e)
        }
        return WeatherData(isSuccess = false)
    }

    private fun fetchFromWttrIn(cityName: String, unit: String): WeatherData {
        try {
            val target = if (cityName.isNotBlank()) URLEncoder.encode(cityName, "UTF-8") else ""
            val url = URL("https://wttr.in/$target?format=j1")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }

            if (conn.responseCode == 200) {
                val body = getUncompressedStream(conn).bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val current = json.getJSONArray("current_condition").getJSONObject(0)
                val nearest = json.getJSONArray("nearest_area").getJSONObject(0)

                val areaName = nearest.getJSONArray("areaName").getJSONObject(0).getString("value")
                val tempC = current.getString("temp_C").toDoubleOrNull() ?: 0.0
                val humidity = current.getString("humidity").toIntOrNull() ?: 0
                val langZh = current.optJSONArray("lang_zh")
                val desc = if (langZh != null && langZh.length() > 0) {
                    langZh.getJSONObject(0).getString("value")
                } else {
                    current.getJSONArray("weatherDesc").getJSONObject(0).getString("value")
                }

                return WeatherData(
                    city = areaName,
                    temp = tempC,
                    unit = unit,
                    description = desc,
                    humidity = humidity,
                    timestamp = System.currentTimeMillis(),
                    isSuccess = true
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "wttr.in fallback failed", e)
        }
        return WeatherData(isSuccess = false)
    }

    private fun getUncompressedStream(conn: HttpURLConnection): InputStream {
        val encoding = conn.contentEncoding
        return if (encoding != null && encoding.contains("gzip", ignoreCase = true)) {
            GZIPInputStream(conn.inputStream)
        } else {
            conn.inputStream
        }
    }

    private fun sanitizeChineseCity(rawCity: String): String {
        var result = rawCity.trim()
        val removeSuffixes = listOf("特别行政区", "壮族自治区", "回族自治区", "维吾尔自治区", "自治区", "自治州", "地区", "盟", "市", "省", "县")
        for (suffix in removeSuffixes) {
            if (result.endsWith(suffix) && result.length > suffix.length) {
                result = result.substring(0, result.length - suffix.length)
                break
            }
        }
        return result
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "晴朗"
            1 -> "晴间多云"
            2 -> "多云"
            3 -> "阴"
            45, 48 -> "雾"
            51, 53, 55 -> "毛毛雨"
            61, 63 -> "小雨"
            65 -> "大雨"
            71, 73, 75 -> "雪"
            80, 81, 82 -> "阵雨"
            95, 96, 99 -> "雷阵雨"
            else -> "多云"
        }
    }
}
