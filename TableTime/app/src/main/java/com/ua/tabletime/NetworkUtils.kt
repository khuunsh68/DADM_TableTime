package com.ua.tabletime

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

object NetworkUtils {

    private const val DEFAULT_BACKOFF_MULT: Float = 1f
    private const val TIMEOUT_MS = 20000
    private const val MAX_RETRIES = DefaultRetryPolicy.DEFAULT_MAX_RETRIES

    fun getDefaultRetryPolicy(): DefaultRetryPolicy {
        return DefaultRetryPolicy(
            TIMEOUT_MS,
            MAX_RETRIES,
            DEFAULT_BACKOFF_MULT
        )
    }

    fun sendJsonObjectRequest(
        context: Context,
        method: Int,
        url: String,
        jsonRequest: JSONObject?,
        onSuccess: (JSONObject) -> Unit,
        onError: (VolleyError) -> Unit,
        headers: Map<String, String>? = null
    ) {
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest = object : JsonObjectRequest(method, url, jsonRequest,
            Response.Listener { response ->
                onSuccess(response)
            },
            Response.ErrorListener { error ->
                onError(error)
            }
        ) {
            override fun getRetryPolicy(): DefaultRetryPolicy {
                return getDefaultRetryPolicy()
            }

            override fun getHeaders(): MutableMap<String, String> {
                return headers?.toMutableMap() ?: super.getHeaders()
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    fun sendJsonArrayRequest(
        context: Context,
        method: Int,
        url: String,
        onSuccess: (JSONArray) -> Unit,
        onError: (VolleyError) -> Unit,
        headers: Map<String, String>? = null
    ) {
        val requestQueue = Volley.newRequestQueue(context)
        val jsonArrayRequest = object : JsonArrayRequest(method, url, null,
            Response.Listener { response ->
                onSuccess(response)
            },
            Response.ErrorListener { error ->
                onError(error)
            }
        ) {
            override fun getRetryPolicy(): DefaultRetryPolicy {
                return getDefaultRetryPolicy()
            }

            override fun getHeaders(): MutableMap<String, String> {
                return headers?.toMutableMap() ?: super.getHeaders()
            }
        }
        requestQueue.add(jsonArrayRequest)
    }
}