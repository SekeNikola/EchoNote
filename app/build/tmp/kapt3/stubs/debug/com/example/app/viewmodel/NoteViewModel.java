package com.example.app.viewmodel;

import androidx.lifecycle.*;
import kotlinx.coroutines.flow.StateFlow;
import com.example.app.data.Note;
import com.example.app.data.NoteRepository;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import com.example.app.audio.AudioRecorder;
import com.example.app.audio.CompressedAudioRecorder;
import com.example.app.network.GPTRequest;
import com.example.app.network.Message;
import com.example.app.network.RetrofitInstance;
import com.example.app.worker.ReminderScheduler;
import com.example.app.util.ApiKeyProvider;
import com.example.app.utils.OpenAITTS;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.io.File;
import java.util.Locale;
import com.example.app.util.NetworkUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.common.InputImage;
import kotlinx.coroutines.Dispatchers;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.jsoup.Jsoup;
import java.net.URL;
import java.io.IOException;
import okhttp3.MultipartBody;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.content.Intent;
import com.example.app.data.ChatMessage;
import com.example.app.data.Task;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import org.json.JSONArray;
import android.util.Base64;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00ca\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b(\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\ba\u0018\u00002\u00020\u00012\u00020\u0002B\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\u001e\u0010X\u001a\u00020\u000e2\u0006\u0010Y\u001a\u00020\u000e2\u0006\u0010Z\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010[J\u0016\u0010\\\u001a\u00020\u000e2\u0006\u0010]\u001a\u00020^H\u0082@\u00a2\u0006\u0002\u0010_J\u0016\u0010`\u001a\u00020a2\u0006\u0010b\u001a\u00020\u000e2\u0006\u0010c\u001a\u00020\u0013J\u000e\u0010d\u001a\u00020e2\u0006\u0010f\u001a\u00020gJ\u000e\u0010h\u001a\u00020e2\u0006\u0010i\u001a\u00020\u000eJ\u0010\u0010j\u001a\u00020\u000e2\u0006\u0010]\u001a\u00020^H\u0002J\u001e\u0010k\u001a\u00020a2\u0006\u0010l\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010[J\u001e\u0010m\u001a\u00020a2\u0006\u0010n\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010[J\u0010\u0010o\u001a\u00020\u000e2\u0006\u0010p\u001a\u00020\u000eH\u0002J\u0010\u0010q\u001a\u00020\u000e2\u0006\u0010p\u001a\u00020\u000eH\u0002J\u0010\u0010r\u001a\u00020\u000e2\u0006\u0010p\u001a\u00020\u000eH\u0002J\u0006\u0010s\u001a\u00020aJ\u0006\u0010t\u001a\u00020eJ\u0006\u0010u\u001a\u00020aJ\u0006\u0010v\u001a\u00020aJ\b\u0010w\u001a\u00020aH\u0002J\u001a\u0010x\u001a\u00020e2\b\b\u0002\u0010y\u001a\u00020\u000e2\b\b\u0002\u0010z\u001a\u00020\u000eJ\u001e\u0010{\u001a\u00020a2\u0006\u0010|\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010[J\b\u0010}\u001a\u00020DH\u0002J\b\u0010~\u001a\u00020eH\u0002J-\u0010\u007f\u001a\u00020e2\u0006\u0010y\u001a\u00020\u000e2\u0007\u0010\u0080\u0001\u001a\u00020\u000e2\t\b\u0002\u0010\u0081\u0001\u001a\u00020\u000e2\t\b\u0002\u0010\u0082\u0001\u001a\u00020gJ\u001f\u0010\u0083\u0001\u001a\u00020a2\u0006\u0010n\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010[J)\u0010\u0084\u0001\u001a\u00020a2\u0007\u0010\u0085\u0001\u001a\u00020\u000e2\u0006\u0010|\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0003\u0010\u0086\u0001J\u000f\u0010\u0087\u0001\u001a\u00020e2\u0006\u0010f\u001a\u00020gJ\u0010\u0010\u0088\u0001\u001a\u00020e2\u0007\u0010\u0089\u0001\u001a\u00020gJ\u0019\u0010\u008a\u0001\u001a\u00020\u00132\u0006\u0010p\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000eH\u0002J(\u0010\u008b\u0001\u001a\u00020\u00132\u0006\u0010p\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000e2\r\u0010\u008c\u0001\u001a\b\u0012\u0004\u0012\u00020\u00100\nH\u0002J\u0007\u0010\u008d\u0001\u001a\u00020aJ$\u0010\u008e\u0001\u001a\u00020\u000e2\b\u0010\u008f\u0001\u001a\u00030\u0090\u00012\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0082@\u00a2\u0006\u0003\u0010\u0093\u0001J\u0018\u0010\u0094\u0001\u001a\b\u0012\u0004\u0012\u00020\u000e0\n2\u0007\u0010\u0095\u0001\u001a\u00020\u000eH\u0002J\u0012\u0010\u0096\u0001\u001a\u00020\u000e2\u0007\u0010\u0097\u0001\u001a\u00020\u000eH\u0002J-\u0010\u0098\u0001\u001a\u0016\u0012\u0004\u0012\u00020\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\n\u0018\u00010\r2\u0007\u0010\u0099\u0001\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0003\u0010\u009a\u0001J!\u0010\u009b\u0001\u001a\u00020\u000e2\u0007\u0010\u009c\u0001\u001a\u00020\u000e2\r\u0010\u008c\u0001\u001a\b\u0012\u0004\u0012\u00020\u00100\nH\u0002J\u0012\u0010\u009d\u0001\u001a\u00020\u000e2\u0007\u0010\u0097\u0001\u001a\u00020\u000eH\u0002J\u0012\u0010\u009e\u0001\u001a\u00020\u000e2\u0007\u0010\u0097\u0001\u001a\u00020\u000eH\u0002J\u0019\u0010\u009f\u0001\u001a\u00020\u000e2\u0007\u0010\u00a0\u0001\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0003\u0010\u009a\u0001J\u0019\u0010\u00a1\u0001\u001a\u00020\u000e2\u0007\u0010\u00a2\u0001\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0003\u0010\u009a\u0001J\u0011\u0010\u00a3\u0001\u001a\u00020\u000e2\u0006\u0010p\u001a\u00020\u000eH\u0002J\u0018\u0010\u00a4\u0001\u001a\u00020\u000e2\u0006\u0010p\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0003\u0010\u009a\u0001J\u0011\u0010\u00a5\u0001\u001a\u00020\u000e2\u0006\u0010p\u001a\u00020\u000eH\u0002J\u0018\u0010\u00a6\u0001\u001a\u00020\u000e2\u0006\u0010O\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0003\u0010\u009a\u0001J\u0010\u0010\u00a7\u0001\u001a\u00020e2\u0007\u0010\u0099\u0001\u001a\u00020\u000eJ\u0017\u0010\u00a8\u0001\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00180>2\u0006\u0010f\u001a\u00020gJ\u000b\u0010\u00a9\u0001\u001a\u0004\u0018\u00010\u000eH\u0002J\u0007\u0010\u00aa\u0001\u001a\u00020aJ\t\u0010\u00ab\u0001\u001a\u00020aH\u0002J\u0011\u0010\u00ac\u0001\u001a\u00020\u00132\u0006\u0010p\u001a\u00020\u000eH\u0002J\u0012\u0010\u00ad\u0001\u001a\u00020\u00132\u0007\u0010\u00ae\u0001\u001a\u00020gH\u0002J\u0012\u0010\u00af\u0001\u001a\u00020\u00132\u0007\u0010\u00b0\u0001\u001a\u00020gH\u0002J\u0010\u0010\u00b1\u0001\u001a\u00020a2\u0007\u0010\u00b2\u0001\u001a\u00020gJ\t\u0010\u00b3\u0001\u001a\u00020eH\u0002J\t\u0010\u00b4\u0001\u001a\u00020eH\u0002J\t\u0010\u00b5\u0001\u001a\u00020eH\u0002J\u0011\u0010\u00b6\u0001\u001a\u00020\u000e2\u0006\u0010p\u001a\u00020\u000eH\u0002J\t\u0010\u00b7\u0001\u001a\u00020aH\u0014J\u0012\u0010\u00b8\u0001\u001a\u00020a2\u0007\u0010\u00b9\u0001\u001a\u00020\'H\u0016J\u0018\u0010\u00ba\u0001\u001a\b\u0012\u0004\u0012\u00020\u00100\n2\u0007\u0010\u0099\u0001\u001a\u00020\u000eH\u0002J\"\u0010\u00bb\u0001\u001a\u00020\u000e2\u0006\u0010]\u001a\u00020^2\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0082@\u00a2\u0006\u0003\u0010\u00bc\u0001J$\u0010\u00bb\u0001\u001a\u00020\u000e2\b\u0010\u00bd\u0001\u001a\u00030\u0090\u00012\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0082@\u00a2\u0006\u0003\u0010\u0093\u0001J\"\u0010\u00be\u0001\u001a\u00020\u000e2\u0006\u0010]\u001a\u00020^2\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0082@\u00a2\u0006\u0003\u0010\u00bc\u0001J$\u0010\u00be\u0001\u001a\u00020\u000e2\b\u0010\u00bd\u0001\u001a\u00030\u0090\u00012\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0082@\u00a2\u0006\u0003\u0010\u0093\u0001J$\u0010\u00bf\u0001\u001a\u00020\'2\b\u0010\u008f\u0001\u001a\u00030\u0090\u00012\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0086@\u00a2\u0006\u0003\u0010\u0093\u0001J \u0010\u00c0\u0001\u001a\u00020a2\u0006\u0010z\u001a\u00020\u000e2\u0007\u0010\u00c1\u0001\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010[J*\u0010\u00c2\u0001\u001a\u00020a2\u0006\u0010O\u001a\u00020\u000e2\u0007\u0010\u00c3\u0001\u001a\u00020\u000e2\u0007\u0010\u00c1\u0001\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0003\u0010\u0086\u0001J\"\u0010\u00c4\u0001\u001a\u00020\'2\u0006\u0010]\u001a\u00020^2\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0086@\u00a2\u0006\u0003\u0010\u00bc\u0001J$\u0010\u00c4\u0001\u001a\u00020\'2\b\u0010\u00bd\u0001\u001a\u00030\u0090\u00012\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0086@\u00a2\u0006\u0003\u0010\u0093\u0001J\u0019\u0010\u00c5\u0001\u001a\u00020\'2\u0007\u0010\u00c6\u0001\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0003\u0010\u009a\u0001J$\u0010\u00c7\u0001\u001a\u00020\'2\b\u0010\u00c8\u0001\u001a\u00030\u0090\u00012\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0086@\u00a2\u0006\u0003\u0010\u0093\u0001J\u0019\u0010\u00c9\u0001\u001a\u00020\'2\u0007\u0010\u00a0\u0001\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0003\u0010\u009a\u0001J\u000f\u0010\u00ca\u0001\u001a\u00020e2\u0006\u0010p\u001a\u00020\u000eJ\u0019\u0010\u00cb\u0001\u001a\u00020\'2\u0007\u0010\u00a2\u0001\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0003\u0010\u009a\u0001J\u000f\u0010\u00cc\u0001\u001a\u00020a2\u0006\u0010f\u001a\u00020gJ\u0015\u0010\u00cd\u0001\u001a\u00020a2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00100\nJ\u0007\u0010\u00ce\u0001\u001a\u00020eJ\u001f\u0010\u00ce\u0001\u001a\u00020a2\u0006\u0010n\u001a\u00020\u000e2\u0006\u0010\u001d\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010[J\u0007\u0010\u00cf\u0001\u001a\u00020eJ\u0010\u0010\u00d0\u0001\u001a\u00020aH\u0082@\u00a2\u0006\u0003\u0010\u00d1\u0001J\u0007\u0010\u00d2\u0001\u001a\u00020eJ\u0007\u0010\u00d3\u0001\u001a\u00020eJ\u0010\u0010\u00d4\u0001\u001a\u00020e2\u0007\u0010\u0097\u0001\u001a\u00020\u000eJ$\u0010\u00d5\u0001\u001a\u00020e2\u0007\u0010\u0097\u0001\u001a\u00020\u000e2\b\u0010\u00bd\u0001\u001a\u00030\u0090\u00012\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001J\u0019\u0010\u00d6\u0001\u001a\u00020a2\u0007\u0010\u00d7\u0001\u001a\u00020.H\u0082@\u00a2\u0006\u0003\u0010\u00d8\u0001J\u0010\u0010\u00d9\u0001\u001a\u00020a2\u0007\u0010\u00da\u0001\u001a\u00020gJ\u0010\u0010\u00db\u0001\u001a\u00020e2\u0007\u0010\u00dc\u0001\u001a\u00020\u000eJ\u0011\u0010\u00dd\u0001\u001a\u00020a2\u0006\u0010p\u001a\u00020\u000eH\u0002J\u0011\u0010\u00de\u0001\u001a\u00020a2\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001J\u0007\u0010\u00df\u0001\u001a\u00020aJ\u0011\u0010\u00e0\u0001\u001a\u00020a2\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001J\u0007\u0010\u00e1\u0001\u001a\u00020aJ\u0007\u0010\u00e2\u0001\u001a\u00020aJ\t\u0010\u00e3\u0001\u001a\u00020aH\u0002J\u0007\u0010\u00e4\u0001\u001a\u00020aJ\u0010\u0010\u00e5\u0001\u001a\u00020e2\u0007\u0010\u00e6\u0001\u001a\u00020\u0018J\u0010\u0010\u00e7\u0001\u001a\u00020e2\u0007\u0010\u0089\u0001\u001a\u00020gJ$\u0010\u00e8\u0001\u001a\u00020\u000e2\b\u0010\u00c8\u0001\u001a\u00030\u0090\u00012\b\u0010\u0091\u0001\u001a\u00030\u0092\u0001H\u0082@\u00a2\u0006\u0003\u0010\u0093\u0001J\u0019\u0010\u00e9\u0001\u001a\u00020e2\u0007\u0010\u00da\u0001\u001a\u00020g2\u0007\u0010\u00ea\u0001\u001a\u00020\u000eJ\u0019\u0010\u00eb\u0001\u001a\u00020e2\u0007\u0010\u00da\u0001\u001a\u00020g2\u0007\u0010\u00ec\u0001\u001a\u00020\u000eJ\u0017\u0010\u00ed\u0001\u001a\u00020e2\u0006\u0010f\u001a\u00020g2\u0006\u0010y\u001a\u00020\u000eJ\u0010\u0010\u00ee\u0001\u001a\u00020aH\u0082@\u00a2\u0006\u0003\u0010\u00d1\u0001J\u0019\u0010\u00ef\u0001\u001a\u00020e2\u0007\u0010\u00da\u0001\u001a\u00020g2\u0007\u0010\u0099\u0001\u001a\u00020\u000eJ3\u0010\u00f0\u0001\u001a\u00020e2\u0007\u0010\u0089\u0001\u001a\u00020g2\u0006\u0010y\u001a\u00020\u000e2\u0007\u0010\u0080\u0001\u001a\u00020\u000e2\u0007\u0010\u0081\u0001\u001a\u00020\u000e2\u0007\u0010\u0082\u0001\u001a\u00020gJ\u0019\u0010\u00f1\u0001\u001a\u00020e2\u0007\u0010\u00da\u0001\u001a\u00020g2\u0007\u0010\u0099\u0001\u001a\u00020\u000eJ\u0011\u0010\u00f2\u0001\u001a\u00020a2\u0006\u0010p\u001a\u00020\u000eH\u0002R\u001a\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010\f\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000e0\r0\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u000e0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00130\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00130\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00130\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00130\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00130\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u000e0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010\u001d\u001a\u0010\u0012\f\u0012\n \u001f*\u0004\u0018\u00010\u000e0\u000e0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u001d\u0010\"\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0#\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u001f\u0010&\u001a\u0010\u0012\f\u0012\n \u001f*\u0004\u0018\u00010\'0\'0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010!R)\u0010)\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000e0\r0\n0#\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010%R\u001d\u0010+\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\n0#\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010%R\u0010\u0010-\u001a\u0004\u0018\u00010.X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010/\u001a\u000200X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u00101\u001a\b\u0012\u0004\u0012\u00020\u000e02X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u00103\u001a\b\u0012\u0004\u0012\u00020\u000e0#\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010%R\u0017\u00105\u001a\b\u0012\u0004\u0012\u00020\u00130#\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010%R\u000e\u00106\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u00107\u001a\b\u0012\u0004\u0012\u00020\u00130#\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010%R\u0017\u00108\u001a\b\u0012\u0004\u0012\u00020\u00130#\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010%R\u001f\u00109\u001a\u0010\u0012\f\u0012\n \u001f*\u0004\u0018\u00010\u00130\u00130\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010!R\u0017\u0010:\u001a\b\u0012\u0004\u0012\u00020\u00130#\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010%R\u001f\u0010;\u001a\u0010\u0012\f\u0012\n \u001f*\u0004\u0018\u00010\u00130\u00130\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010!R\u000e\u0010<\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010=\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\n0>\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010@R\u0010\u0010A\u001a\u0004\u0018\u00010BX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010C\u001a\u0004\u0018\u00010DX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010E\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00180\n0#\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u0010%R\u001f\u0010G\u001a\u0010\u0012\f\u0012\n \u001f*\u0004\u0018\u00010\u000e0\u000e0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\bH\u0010!R\u0017\u0010I\u001a\b\u0012\u0004\u0012\u00020\u00130#\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u0010%R\u0017\u0010K\u001a\b\u0012\u0004\u0012\u00020\u00130#\u00a2\u0006\b\n\u0000\u001a\u0004\bL\u0010%R\u0010\u0010M\u001a\u0004\u0018\u00010NX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001f\u0010O\u001a\u0010\u0012\f\u0012\n \u001f*\u0004\u0018\u00010\u000e0\u000e0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\bP\u0010!R\u0010\u0010Q\u001a\u0004\u0018\u00010RX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010S\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010T\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\n0#\u00a2\u0006\b\n\u0000\u001a\u0004\bU\u0010%R\u0017\u0010V\u001a\b\u0012\u0004\u0012\u00020\u000e0#\u00a2\u0006\b\n\u0000\u001a\u0004\bW\u0010%\u00a8\u0006\u00f3\u0001"}, d2 = {"Lcom/example/app/viewmodel/NoteViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "Landroid/speech/tts/TextToSpeech$OnInitListener;", "repository", "Lcom/example/app/data/NoteRepository;", "app", "Landroid/app/Application;", "(Lcom/example/app/data/NoteRepository;Landroid/app/Application;)V", "_allTasks", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/app/data/Task;", "_assistantChatHistory", "Lkotlin/Pair;", "", "_chatMessages", "Lcom/example/app/data/ChatMessage;", "_fullTranscript", "_isAiLoading", "", "_isListening", "_isProcessing", "_isSpeaking", "_savedChats", "Lcom/example/app/data/Note;", "_shouldAutoRestart", "_shouldCloseAssistant", "_voiceSessionHistory", "_voiceText", "aiResponse", "Landroidx/lifecycle/MutableLiveData;", "kotlin.jvm.PlatformType", "getAiResponse", "()Landroidx/lifecycle/MutableLiveData;", "allTasks", "Lkotlinx/coroutines/flow/StateFlow;", "getAllTasks", "()Lkotlinx/coroutines/flow/StateFlow;", "amplitude", "", "getAmplitude", "assistantChatHistory", "getAssistantChatHistory", "chatMessages", "getChatMessages", "compressedAudioFile", "Ljava/io/File;", "compressedAudioRecorder", "Lcom/example/app/audio/CompressedAudioRecorder;", "currentListItems", "", "fullTranscript", "getFullTranscript", "isAiLoading", "isCreatingList", "isListening", "isProcessing", "isRecording", "isSpeaking", "isVoiceOverlayVisible", "listType", "notes", "Landroidx/lifecycle/LiveData;", "getNotes", "()Landroidx/lifecycle/LiveData;", "openAITTS", "Lcom/example/app/utils/OpenAITTS;", "recognizerIntent", "Landroid/content/Intent;", "savedChats", "getSavedChats", "searchQuery", "getSearchQuery", "shouldAutoRestart", "getShouldAutoRestart", "shouldCloseAssistant", "getShouldCloseAssistant", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "summary", "getSummary", "tts", "Landroid/speech/tts/TextToSpeech;", "ttsReady", "voiceSessionHistory", "getVoiceSessionHistory", "voiceText", "getVoiceText", "analyzeImageWithOpenAI", "base64Image", "extractedText", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "analyzeImageWithVision", "bitmap", "Landroid/graphics/Bitmap;", "(Landroid/graphics/Bitmap;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "appendTranscript", "", "newText", "isFinal", "archiveNote", "Lkotlinx/coroutines/Job;", "id", "", "askAssistant", "question", "bitmapToBase64", "checkForListCompletion", "userInput", "checkForNoteTaskCreation", "userMessage", "cleanBracketsFromText", "text", "cleanExtractedText", "cleanOCRForTranscript", "clearAssistantChat", "clearChatHistory", "clearVoiceSession", "clearVoiceText", "compressAndSendAudioToOpenAI", "createNote", "title", "content", "createNoteFromVoice", "originalText", "createRecognizerIntent", "createSampleTasks", "createTask", "description", "priority", "dueDate", "createTaskFromChat", "createTaskFromVoiceContext", "taskContent", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteNote", "deleteTask", "taskId", "detectNoteIntent", "detectTaskIntent", "recentHistory", "endVoiceSession", "extractDocumentText", "documentUri", "Landroid/net/Uri;", "context", "Landroid/content/Context;", "(Landroid/net/Uri;Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractListItems", "input", "extractMainActionFromMessage", "message", "extractSummaryAndTasksWithOpenAI", "transcript", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractTaskFromConversation", "currentText", "extractTaskFromMessage", "extractTaskFromMessageContent", "extractVideoSummary", "videoUrl", "fetchWebPageContent", "webUrl", "generateFallbackTitle", "generateSmartTitle", "generateSummary", "generateTitle", "getAssistantResponse", "getNoteById", "getRawAudioFilePath", "hideVoiceOverlay", "initializeTTS", "isTaskRelated", "isTaskToday", "date", "isToday", "timestamp", "loadChatForContinuation", "chatId", "loadChatMessages", "loadTasks", "loadTasksOnce", "makeTextMoreNatural", "onCleared", "onInit", "status", "parseTranscriptToChatMessages", "performOCR", "(Landroid/graphics/Bitmap;Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "imageUri", "performOCRRaw", "processDocument", "processExtractedContent", "contentType", "processImageContent", "ocrText", "processImageWithOCR", "processTextNote", "textContent", "processUploadedAudio", "audioUri", "processVideoUrl", "processVoiceCommand", "processWebPageUrl", "readAloud", "saveChatAsChat", "saveChatAsNote", "saveChatAsTask", "saveListAsNote", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveTestChatNote", "saveVoiceSessionAsNote", "sendChatMessage", "sendChatMessageWithImage", "sendCompressedAudioToOpenAI", "file", "(Ljava/io/File;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setReminder", "noteId", "showVoiceOverlay", "query", "speakText", "startListening", "startNewVoiceSession", "startSpeechRecognition", "stopAndSaveNote", "stopListening", "stopSpeaking", "stopSpeechRecognition", "toggleFavorite", "note", "toggleTaskComplete", "transcribeUploadedAudio", "updateChecklistState", "checklistState", "updateNoteSnippet", "snippet", "updateNoteTitle", "updateSavedChats", "updateSummaryWithOpenAI", "updateTask", "updateTranscript", "useAndroidTTS", "app_debug"})
public final class NoteViewModel extends androidx.lifecycle.AndroidViewModel implements android.speech.tts.TextToSpeech.OnInitListener {
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.data.NoteRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.app.audio.CompressedAudioRecorder compressedAudioRecorder = null;
    @org.jetbrains.annotations.Nullable()
    private java.io.File compressedAudioFile;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Note>> notes = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> searchQuery = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isRecording = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> summary = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isVoiceOverlayVisible = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> aiResponse = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Integer> amplitude = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _fullTranscript = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> fullTranscript = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<kotlin.Pair<java.lang.String, java.lang.String>>> _assistantChatHistory = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<kotlin.Pair<java.lang.String, java.lang.String>>> assistantChatHistory = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _shouldCloseAssistant = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> shouldCloseAssistant = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.app.data.ChatMessage>> _chatMessages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.ChatMessage>> chatMessages = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isAiLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isAiLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isListening = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isListening = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _voiceText = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> voiceText = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isProcessing = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isProcessing = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isSpeaking = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSpeaking = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.app.data.ChatMessage>> _voiceSessionHistory = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.ChatMessage>> voiceSessionHistory = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _shouldAutoRestart = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> shouldAutoRestart = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.app.data.Task>> _allTasks = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.Task>> allTasks = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.app.data.Note>> _savedChats = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.Note>> savedChats = null;
    @org.jetbrains.annotations.Nullable()
    private android.speech.SpeechRecognizer speechRecognizer;
    @org.jetbrains.annotations.Nullable()
    private android.speech.tts.TextToSpeech tts;
    private boolean ttsReady = false;
    @org.jetbrains.annotations.Nullable()
    private com.example.app.utils.OpenAITTS openAITTS;
    @org.jetbrains.annotations.Nullable()
    private android.content.Intent recognizerIntent;
    private boolean isCreatingList = false;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<java.lang.String> currentListItems;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String listType = "";
    
    public NoteViewModel(@org.jetbrains.annotations.NotNull()
    com.example.app.data.NoteRepository repository, @org.jetbrains.annotations.NotNull()
    android.app.Application app) {
        super(null);
    }
    
    /**
     * Get an AI assistant response from OpenAI for the given transcript.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job getAssistantResponse(@org.jetbrains.annotations.NotNull()
    java.lang.String transcript) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateSummaryWithOpenAI(long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String transcript) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTranscript(long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String transcript) {
        return null;
    }
    
    /**
     * Use OpenAI GPT to extract summary and tasks from transcript.
     * The prompt asks for a JSON response: {"summary": "...", "tasks": [ ... ]}
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object extractSummaryAndTasksWithOpenAI(@org.jetbrains.annotations.NotNull()
    java.lang.String transcript, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Pair<java.lang.String, ? extends java.util.List<java.lang.String>>> $completion) {
        return null;
    }
    
    private final java.lang.String cleanBracketsFromText(java.lang.String text) {
        return null;
    }
    
    private final java.lang.String cleanExtractedText(java.lang.String text) {
        return null;
    }
    
    private final java.lang.String cleanOCRForTranscript(java.lang.String text) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateNoteSnippet(long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String snippet) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateChecklistState(long noteId, @org.jetbrains.annotations.NotNull()
    java.lang.String checklistState) {
        return null;
    }
    
    public final void stopAndSaveNote() {
    }
    
    private final void compressAndSendAudioToOpenAI() {
    }
    
    private final java.lang.Object sendCompressedAudioToOpenAI(java.io.File file, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String getRawAudioFilePath() {
        return null;
    }
    
    public final void setReminder(long noteId) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job deleteNote(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.example.app.data.Note>> getNotes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getSearchQuery() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isRecording() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getSummary() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.Boolean> isVoiceOverlayVisible() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.String> getAiResponse() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.MutableLiveData<java.lang.Integer> getAmplitude() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getFullTranscript() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<kotlin.Pair<java.lang.String, java.lang.String>>> getAssistantChatHistory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShouldCloseAssistant() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.ChatMessage>> getChatMessages() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isAiLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isListening() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getVoiceText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isProcessing() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSpeaking() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.ChatMessage>> getVoiceSessionHistory() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShouldAutoRestart() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.Task>> getAllTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.Note>> getSavedChats() {
        return null;
    }
    
    public final void appendTranscript(@org.jetbrains.annotations.NotNull()
    java.lang.String newText, boolean isFinal) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateTitle(@org.jetbrains.annotations.NotNull()
    java.lang.String summary, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job toggleFavorite(@org.jetbrains.annotations.NotNull()
    com.example.app.data.Note note) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.example.app.data.Note> getNoteById(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateNoteTitle(long id, @org.jetbrains.annotations.NotNull()
    java.lang.String title) {
        return null;
    }
    
    public final void readAloud(long id) {
    }
    
    @java.lang.Override()
    public void onInit(int status) {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job archiveNote(long id) {
        return null;
    }
    
    public final void startSpeechRecognition(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final android.content.Intent createRecognizerIntent() {
        return null;
    }
    
    private final java.lang.String generateSummary(java.lang.String text) {
        return null;
    }
    
    public final void stopSpeechRecognition() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job showVoiceOverlay(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
        return null;
    }
    
    public final void hideVoiceOverlay() {
    }
    
    public final void clearAssistantChat() {
    }
    
    private final java.lang.Object checkForListCompletion(java.lang.String userInput, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.util.List<java.lang.String> extractListItems(java.lang.String input) {
        return null;
    }
    
    private final java.lang.Object saveListAsNote(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job saveChatAsNote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job saveChatAsTask() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processUploadedAudio(@org.jetbrains.annotations.NotNull()
    android.net.Uri audioUri, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object transcribeUploadedAudio(android.net.Uri audioUri, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processImageWithOCR(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processImageWithOCR(@org.jetbrains.annotations.NotNull()
    android.net.Uri imageUri, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object performOCR(android.graphics.Bitmap bitmap, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object performOCRRaw(android.graphics.Bitmap bitmap, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object performOCR(android.net.Uri imageUri, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object performOCRRaw(android.net.Uri imageUri, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String bitmapToBase64(android.graphics.Bitmap bitmap) {
        return null;
    }
    
    private final java.lang.Object analyzeImageWithOpenAI(java.lang.String base64Image, java.lang.String extractedText, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object analyzeImageWithVision(android.graphics.Bitmap bitmap, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processTextNote(@org.jetbrains.annotations.NotNull()
    java.lang.String textContent, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processVideoUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String videoUrl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object extractVideoSummary(java.lang.String videoUrl, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processWebPageUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String webUrl, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchWebPageContent(java.lang.String webUrl, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object processDocument(@org.jetbrains.annotations.NotNull()
    android.net.Uri documentUri, @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object extractDocumentText(android.net.Uri documentUri, android.content.Context context, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object processExtractedContent(java.lang.String content, java.lang.String contentType, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object processImageContent(java.lang.String summary, java.lang.String ocrText, java.lang.String contentType, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job saveTestChatNote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job askAssistant(@org.jetbrains.annotations.NotNull()
    java.lang.String question) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job sendChatMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
        return null;
    }
    
    private final java.lang.Object checkForNoteTaskCreation(java.lang.String userMessage, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object createTaskFromChat(java.lang.String userMessage, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String extractTaskFromMessage(java.lang.String message) {
        return null;
    }
    
    private final java.lang.String extractTaskFromMessageContent(java.lang.String message) {
        return null;
    }
    
    private final java.lang.Object saveChatAsNote(java.lang.String userMessage, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job sendChatMessageWithImage(@org.jetbrains.annotations.NotNull()
    java.lang.String message, @org.jetbrains.annotations.NotNull()
    android.net.Uri imageUri, @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job clearChatHistory() {
        return null;
    }
    
    public final void clearVoiceSession() {
    }
    
    public final void startListening(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void stopListening() {
    }
    
    public final void clearVoiceText() {
    }
    
    public final void startNewVoiceSession() {
    }
    
    public final void endVoiceSession() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job saveVoiceSessionAsNote() {
        return null;
    }
    
    private final void speakText(java.lang.String text) {
    }
    
    private final void useAndroidTTS(java.lang.String text) {
    }
    
    private final java.lang.String makeTextMoreNatural(java.lang.String text) {
        return null;
    }
    
    private final void stopSpeaking() {
    }
    
    private final void initializeTTS() {
    }
    
    private final boolean isTaskRelated(java.lang.String text) {
        return false;
    }
    
    private final java.lang.Object createNoteFromVoice(java.lang.String originalText, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object generateSmartTitle(java.lang.String text, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.String generateFallbackTitle(java.lang.String text) {
        return null;
    }
    
    private final boolean detectTaskIntent(java.lang.String text, java.lang.String aiResponse, java.util.List<com.example.app.data.ChatMessage> recentHistory) {
        return false;
    }
    
    private final boolean detectNoteIntent(java.lang.String text, java.lang.String aiResponse) {
        return false;
    }
    
    private final java.lang.String extractTaskFromConversation(java.lang.String currentText, java.util.List<com.example.app.data.ChatMessage> recentHistory) {
        return null;
    }
    
    private final java.lang.String extractMainActionFromMessage(java.lang.String message) {
        return null;
    }
    
    private final java.lang.Object createTaskFromVoiceContext(java.lang.String taskContent, java.lang.String originalText, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job processVoiceCommand(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return null;
    }
    
    private final kotlinx.coroutines.Job loadTasksOnce() {
        return null;
    }
    
    private final kotlinx.coroutines.Job loadTasks() {
        return null;
    }
    
    private final boolean isTaskToday(long date) {
        return false;
    }
    
    private final kotlinx.coroutines.Job loadChatMessages() {
        return null;
    }
    
    private final kotlinx.coroutines.Job createSampleTasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job createTask(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String priority, long dueDate) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job createNote(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String content) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job toggleTaskComplete(long taskId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job deleteTask(long taskId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTask(long taskId, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String priority, long dueDate) {
        return null;
    }
    
    private final boolean isToday(long timestamp) {
        return false;
    }
    
    public final void loadChatForContinuation(long chatId) {
    }
    
    public final void saveChatAsChat(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.ChatMessage> chatMessages) {
    }
    
    private final java.lang.Object updateSavedChats(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.util.List<com.example.app.data.ChatMessage> parseTranscriptToChatMessages(java.lang.String transcript) {
        return null;
    }
}