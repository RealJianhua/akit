package wenjh.akit.common.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import wenjh.akit.common.util.ContextUtil;
import wenjh.akit.common.util.IOUtils;
import wenjh.akit.common.util.LogUtil;

public class HttpClient {
    private static LogUtil LOG = new LogUtil("HttpClient");
    OkHttpClient httpClient = null;
    private static OkHttpClient okHttpClientSingleton = null;

    public HttpClient() {
        this.httpClient = getSingleInstance();
    }

    static OkHttpClient getSingleInstance() {
        if(okHttpClientSingleton == null) {
            synchronized (HttpClient.class) {
                if(okHttpClientSingleton == null) {
                    okHttpClientSingleton = new OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return okHttpClientSingleton;
    }

    protected OkHttpClient newHttpClient(OkHttpClient.Builder builder) {
        if(builder == null) {
            builder = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS);
        }
        this.httpClient = builder.build();
        return httpClient;
    }

    protected  String callPost(String urlString, Map<String, String> formData) throws Exception {
        return callPost(urlString, formData, null, null);
    }

    protected  String callPost(String urlString, Map<String, String> formData, HttpFormFile[] files, Map<String, String> headers) throws Exception {
        try {
            Response response = realCallPost(urlString, formData, files, headers, httpClient);
            String result = response.body().string();
            LOG.i("<--"+urlString+": \n" + result);
            checkServerResponseStatusCode(result);
            return result;
        } catch (InterruptedIOException e) {
            throw new HttpTimeoutException();
        } catch (Exception e) {
            throw new NetworkBaseException(e);
        }
    }

    static Response realCallPost(String urlString, Map<String, String> formData, HttpFormFile[] files, Map<String, String> headers, OkHttpClient client) throws IOException, NetworkBaseException {
        if(!ContextUtil.isNetworkAvailable()) {
            throw new NetworkUnavailableException();
        }
        LOG.i("-->"+urlString);

        RequestBody requestBody = null;
        Request.Builder requestBuilder = new Request.Builder().url(urlString);

        if (headers != null) {
            for (String key : headers.keySet()) {
                requestBuilder.addHeader(key, headers.get(key));
            }
        }

        if (files != null && files.length > 0) {
            // multipart/form-data
            MultipartBody.Builder kvfbuilder = new MultipartBody.Builder();
            for (HttpFormFile formFile : files) {
                RequestBody rb = new HttpFormFile.FormFileRequestBody(formFile);
                kvfbuilder.addFormDataPart(formFile.getParameterName(), formFile.getFilname(), rb);
            }

            if (formData != null) {
                for (String key : formData.keySet()) {
                    kvfbuilder.addFormDataPart(key, formData.get(key));
                }
            }
            requestBody = kvfbuilder.build();
        } else if (formData != null) {
            // application/x-www-form-urlencoded
            FormBody.Builder kvbuilder = new FormBody.Builder();
            for (String key : formData.keySet()) {
                kvbuilder.add(key, formData.get(key));
            }
            requestBody = kvbuilder.build();
        }

        Request request = requestBuilder.post(requestBody).build();
        Response response = client.newCall(request).execute();
        if (response.code() >= 300) {
            response.body().close();
            throw new HttpResponseStatusErrorException(response.code());
        }
        return response;
    }

    protected String callGet(String urlString, Map<String, String> params) throws Exception {
        return callGet(urlString, params, null);
    }

    protected String callGet(String urlString, Map<String, String> params, Map<String, String> headers) throws Exception {
        try {
            Response response = realCallGet(urlString, params, headers, httpClient);
            String result = response.body().string();
            LOG.i("<--"+urlString+": \n" + result);
            checkServerResponseStatusCode(result);
            return result;
        } catch (InterruptedIOException e) {
            throw new HttpTimeoutException();
        } catch (Exception e) {
            throw new NetworkBaseException(e);
        }
    }

    static Response realCallGet(String urlString, Map<String, String> params, Map<String, String> headers, OkHttpClient client) throws IOException, NetworkBaseException {
        if(!ContextUtil.isNetworkAvailable()) {
            throw new NetworkUnavailableException();
        }
        LOG.i("-->"+urlString);

        Request.Builder builder = new Request.Builder();

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlString = URLProcessUtil.appendParameter(urlString, entry.getKey(), entry.getValue());
            }
        }

        if (headers != null) {
            for (String key : headers.keySet()) {
                builder.addHeader(key, headers.get(key));
            }
        }

        builder.url(urlString);
        builder.get();
        Response response = client.newCall(builder.build()).execute();
        if (response.code() >= 300) {
            response.body().close();
            throw new HttpResponseStatusErrorException(response.code());
        }
        return response;
    }

    public void saveFile(String url, File file, Map<String, String> params, DownloadProgressCallback progress) throws Exception {
        long downloadSize = 0;
        long contentLength = 0;
        boolean fileBeforeDownloadExists = file.exists();

        try {
            LOG.i("save->" + url);

            if (progress != null) {
                progress.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_INIT);
            }

            Response response = realCallGet(url, params, null, httpClient);
            InputStream is = response.body().byteStream();
            contentLength = response.body().contentLength();

            // 中途被停止了
            if (progress != null && progress.getControllerStatus() == DownloadProgressCallback.STATUS_STOP) {
                progress.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_STOP);
                IOUtils.closeQuietly(is);
                response.body().close();
                return;
            }

            if (progress != null) {
                progress.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_PROGRESS);
            }

            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            try {
                byte[] buffer = new byte[4096];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                    downloadSize += len;
                    if (progress != null) {
                        progress.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_PROGRESS);
                        // 中途停止
                        if (progress.getControllerStatus() == DownloadProgressCallback.STATUS_STOP) {
                            progress.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_STOP);
                            break;
                        }
                    }
                }
                os.flush();

                if (progress != null && progress.getControllerStatus() != DownloadProgressCallback.STATUS_STOP) {
                    progress.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_FINISH);
                }

            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
                response.body().close();
            }
        } catch (Exception e) {
            // 删除临时文件
            if (!fileBeforeDownloadExists && file.exists()) {
                file.delete();
            }
            if (progress != null) {
                progress.callback(contentLength, downloadSize, DownloadProgressCallback.STATUS_ERROR);
            }
            throw e;
        }
    }

    void checkServerResponseStatusCode(String result) throws HttpServerReturnedException, JSONException {
        JSONObject json = new JSONObject(result);
        int ec = json.optInt("ec");
        if (ec > 0) {
            String errmsg = json.optString("em");
            throw new HttpServerReturnedException(errmsg, ec, result);
        }
    }

    protected static String[] toJavaArray(JSONArray jsonArray) {
        if (jsonArray != null) {
            String[] array = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                array[i] = jsonArray.optString(i);
            }
            return array;
        }
        return null;
    }

    protected static List<String> toJavaStringList(JSONArray jsonArray) {
        if (jsonArray != null) {
            List<String> list = new java.util.ArrayList<String>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.optString(i));
            }
            return list;
        }
        return null;
    }
}
