package com.example.app.viewmodel;

import androidx.lifecycle.*;
import kotlinx.coroutines.flow.StateFlow;
import com.example.app.data.Note;
import com.example.app.data.Reminder;
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
import com.example.app.network.TTSRequest;
import com.example.app.worker.ReminderScheduler;
import com.example.app.util.ApiKeyProvider;
import com.example.app.utils.OpenAITTS;
import com.example.app.server.ServerTask;
import com.example.app.server.ServerNote;
import com.example.app.server.KtorServer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.io.File;
import java.util.Locale;
import java.util.UUID;
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
import com.example.app.data.CheckboxItem;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import org.json.JSONArray;
import android.util.Base64;
import androidx.work.WorkManager;
import com.example.app.worker.ReminderWorker;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00f4\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b?\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\br\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u00012\u00020\u0002:\u0004\u00b8\u0002\u00b9\u0002B\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\"\u0010i\u001a\u00020j2\u0006\u0010k\u001a\u00020\u00102\u0006\u0010l\u001a\u00020\u00102\n\b\u0002\u0010m\u001a\u0004\u0018\u00010\u0010J\u001e\u0010n\u001a\u00020\u00102\u0006\u0010o\u001a\u00020\u00102\u0006\u0010p\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010qJ\u0016\u0010r\u001a\u00020\u00102\u0006\u0010s\u001a\u00020tH\u0082@\u00a2\u0006\u0002\u0010uJ\u0016\u0010v\u001a\u00020w2\u0006\u0010x\u001a\u00020\u00102\u0006\u0010y\u001a\u00020\u0016J\u000e\u0010z\u001a\u00020j2\u0006\u0010{\u001a\u00020|J\u000e\u0010}\u001a\u00020j2\u0006\u0010~\u001a\u00020\u0010J\u0010\u0010\u007f\u001a\u00020\u00102\u0006\u0010s\u001a\u00020tH\u0002J&\u0010\u0080\u0001\u001a\u00020w2\u0007\u0010\u0081\u0001\u001a\u00020|2\u000b\b\u0002\u0010\u0082\u0001\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0003\u0010\u0083\u0001J \u0010\u0084\u0001\u001a\u00020w2\u0007\u0010\u0085\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010qJ-\u0010\u0086\u0001\u001a\u00020w2\u0007\u0010\u0087\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u00102\n\b\u0002\u0010m\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0003\u0010\u0088\u0001J\u0007\u0010\u0089\u0001\u001a\u00020jJ\u0007\u0010\u008a\u0001\u001a\u00020jJ\u0012\u0010\u008b\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u0012\u0010\u008d\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u0012\u0010\u008e\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u0007\u0010\u008f\u0001\u001a\u00020wJ\u0007\u0010\u0090\u0001\u001a\u00020jJ\u0007\u0010\u0091\u0001\u001a\u00020wJ\u0007\u0010\u0092\u0001\u001a\u00020wJ\u0010\u0010\u0093\u0001\u001a\u00020j2\u0007\u0010\u0094\u0001\u001a\u00020|J\t\u0010\u0095\u0001\u001a\u00020wH\u0002J\u0014\u0010\u0096\u0001\u001a\u0004\u0018\u00010\u00102\u0007\u0010\u0097\u0001\u001a\u00020\u0010H\u0002J\u0007\u0010\u0098\u0001\u001a\u00020jJ\'\u0010\u0099\u0001\u001a\u00020j2\u0007\u0010\u0087\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u00102\r\u0010\u009a\u0001\u001a\b\u0012\u0004\u0012\u00020\u00100\nJ\'\u0010\u009b\u0001\u001a\u00020j2\b\b\u0002\u0010k\u001a\u00020\u00102\b\b\u0002\u0010l\u001a\u00020\u00102\n\b\u0002\u0010m\u001a\u0004\u0018\u00010\u0010J \u0010\u009c\u0001\u001a\u00020w2\u0007\u0010\u009d\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010qJ\t\u0010\u009e\u0001\u001a\u00020QH\u0002J\u0007\u0010\u009f\u0001\u001a\u00020jJ#\u0010\u00a0\u0001\u001a\u00020j2\u0006\u0010k\u001a\u00020\u00102\t\b\u0002\u0010\u00a1\u0001\u001a\u00020\u00102\u0007\u0010\u00a2\u0001\u001a\u00020|J \u0010\u00a3\u0001\u001a\u00020w2\u0007\u0010\u0087\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010qJ\t\u0010\u00a4\u0001\u001a\u00020jH\u0002J.\u0010\u00a5\u0001\u001a\u00020j2\u0006\u0010k\u001a\u00020\u00102\u0007\u0010\u00a1\u0001\u001a\u00020\u00102\t\b\u0002\u0010\u00a6\u0001\u001a\u00020\u00102\t\b\u0002\u0010\u00a7\u0001\u001a\u00020|J \u0010\u00a8\u0001\u001a\u00020w2\u0007\u0010\u0087\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010qJ*\u0010\u00a9\u0001\u001a\u00020w2\u0007\u0010\u00aa\u0001\u001a\u00020\u00102\u0007\u0010\u009d\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0003\u0010\u0088\u0001J\u0012\u0010\u00ab\u0001\u001a\u00020j2\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u000f\u0010\u00ac\u0001\u001a\u00020j2\u0006\u0010{\u001a\u00020|J\u0010\u0010\u00ad\u0001\u001a\u00020j2\u0007\u0010\u0094\u0001\u001a\u00020|J\u0010\u0010\u00ae\u0001\u001a\u00020j2\u0007\u0010\u00af\u0001\u001a\u00020|J\u0019\u0010\u00b0\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0003\u0010\u00b1\u0001J \u0010\u00b2\u0001\u001a\b\u0012\u0004\u0012\u00020\u00100\n2\u0007\u0010\u0087\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u0010H\u0002J\u001a\u0010\u00b3\u0001\u001a\u00020\u00162\u0007\u0010\u008c\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u0010H\u0002J\u0012\u0010\u00b4\u0001\u001a\u00020\u00162\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J)\u0010\u00b5\u0001\u001a\u00020\u00162\u0007\u0010\u008c\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u00102\r\u0010\u00b6\u0001\u001a\b\u0012\u0004\u0012\u00020\u00120\nH\u0002J\u0012\u0010\u00b7\u0001\u001a\u00020\u00162\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u0007\u0010\u00b8\u0001\u001a\u00020wJ\u0007\u0010\u00b9\u0001\u001a\u00020wJ\u0007\u0010\u00ba\u0001\u001a\u00020wJ\u0011\u0010\u00bb\u0001\u001a\u00030\u00bc\u0001H\u0086@\u00a2\u0006\u0003\u0010\u00bd\u0001J$\u0010\u00be\u0001\u001a\u00020\u00102\b\u0010\u00bf\u0001\u001a\u00030\u00c0\u00012\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0082@\u00a2\u0006\u0003\u0010\u00c3\u0001J\u0018\u0010\u00c4\u0001\u001a\b\u0012\u0004\u0012\u00020\u00100\n2\u0007\u0010\u00c5\u0001\u001a\u00020\u0010H\u0002J\u0012\u0010\u00c6\u0001\u001a\u00020\u00102\u0007\u0010\u00c7\u0001\u001a\u00020\u0010H\u0002J\u001b\u0010\u00c8\u0001\u001a\u0004\u0018\u00010\u00102\u0007\u0010\u00c9\u0001\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0003\u0010\u00b1\u0001J\u0012\u0010\u00ca\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u0012\u0010\u00cb\u0001\u001a\u00020\u00102\u0007\u0010\u00c7\u0001\u001a\u00020\u0010H\u0002J\u0012\u0010\u00cc\u0001\u001a\u00020|2\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J-\u0010\u00cd\u0001\u001a\u0016\u0012\u0004\u0012\u00020\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\n\u0018\u00010\u000f2\u0007\u0010\u00c9\u0001\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0003\u0010\u00b1\u0001J!\u0010\u00ce\u0001\u001a\u00020\u00102\u0007\u0010\u00cf\u0001\u001a\u00020\u00102\r\u0010\u00b6\u0001\u001a\b\u0012\u0004\u0012\u00020\u00120\nH\u0002J\u0012\u0010\u00d0\u0001\u001a\u00020\u00102\u0007\u0010\u00c7\u0001\u001a\u00020\u0010H\u0002J\u0012\u0010\u00d1\u0001\u001a\u00020\u00102\u0007\u0010\u00c7\u0001\u001a\u00020\u0010H\u0002J\u0019\u0010\u00d2\u0001\u001a\u00020\u00102\u0007\u0010\u00d3\u0001\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0003\u0010\u00b1\u0001J\u0019\u0010\u00d4\u0001\u001a\u00020\u00102\u0007\u0010\u00d5\u0001\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0003\u0010\u00b1\u0001J\u0012\u0010\u00d6\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u0019\u0010\u00d7\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0003\u0010\u00b1\u0001J\u0012\u0010\u00d8\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u0018\u0010\u00d9\u0001\u001a\u00020\u00102\u0006\u0010^\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0003\u0010\u00b1\u0001J\u0010\u0010\u00da\u0001\u001a\u00020j2\u0007\u0010\u00c9\u0001\u001a\u00020\u0010J\t\u0010\u00db\u0001\u001a\u00020\u0010H\u0002J\u0017\u0010\u00dc\u0001\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010I0H2\u0006\u0010{\u001a\u00020|J\u000b\u0010\u00dd\u0001\u001a\u0004\u0018\u00010\u0010H\u0002J\u0013\u0010\u00de\u0001\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100aJ\u0007\u0010\u00df\u0001\u001a\u00020wJ#\u0010\u00e0\u0001\u001a\u00020w2\b\u0010\u00e1\u0001\u001a\u00030\u00bc\u00012\u0007\u0010\u00e2\u0001\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0003\u0010\u00e3\u0001J\t\u0010\u00e4\u0001\u001a\u00020wH\u0002J\u0012\u0010\u00e5\u0001\u001a\u00020\u00162\u0007\u0010\u00c7\u0001\u001a\u00020\u0010H\u0002J\u0012\u0010\u00e6\u0001\u001a\u00020\u00162\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\u0012\u0010\u00e7\u0001\u001a\u00020\u00162\u0007\u0010\u00e8\u0001\u001a\u00020|H\u0002J\u0012\u0010\u00e9\u0001\u001a\u00020\u00162\u0007\u0010\u00ea\u0001\u001a\u00020|H\u0002J\t\u0010\u00eb\u0001\u001a\u00020jH\u0002J\t\u0010\u00ec\u0001\u001a\u00020jH\u0002J\t\u0010\u00ed\u0001\u001a\u00020jH\u0002J\t\u0010\u00ee\u0001\u001a\u00020jH\u0002J\u0012\u0010\u00ef\u0001\u001a\u00020\u00102\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J\t\u0010\u00f0\u0001\u001a\u00020wH\u0014J\u0012\u0010\u00f1\u0001\u001a\u00020w2\u0007\u0010\u00f2\u0001\u001a\u00020/H\u0016J\u0012\u0010\u00f3\u0001\u001a\u00020|2\u0007\u0010\u00c7\u0001\u001a\u00020\u0010H\u0002J\"\u0010\u00f4\u0001\u001a\u00020\u00102\u0006\u0010s\u001a\u00020t2\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0082@\u00a2\u0006\u0003\u0010\u00f5\u0001J#\u0010\u00f4\u0001\u001a\u00020\u00102\u0007\u0010m\u001a\u00030\u00c0\u00012\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0082@\u00a2\u0006\u0003\u0010\u00c3\u0001J\"\u0010\u00f6\u0001\u001a\u00020\u00102\u0006\u0010s\u001a\u00020t2\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0082@\u00a2\u0006\u0003\u0010\u00f5\u0001J#\u0010\u00f6\u0001\u001a\u00020\u00102\u0007\u0010m\u001a\u00030\u00c0\u00012\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0082@\u00a2\u0006\u0003\u0010\u00c3\u0001J\u0012\u0010\u00f7\u0001\u001a\u00020w2\u0007\u0010\u00f8\u0001\u001a\u000206H\u0002J$\u0010\u00f9\u0001\u001a\u00020/2\b\u0010\u00bf\u0001\u001a\u00030\u00c0\u00012\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0086@\u00a2\u0006\u0003\u0010\u00c3\u0001J \u0010\u00fa\u0001\u001a\u00020w2\u0006\u0010l\u001a\u00020\u00102\u0007\u0010\u00fb\u0001\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010qJ*\u0010\u00fc\u0001\u001a\u00020w2\u0006\u0010^\u001a\u00020\u00102\u0007\u0010\u00fd\u0001\u001a\u00020\u00102\u0007\u0010\u00fb\u0001\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0003\u0010\u0088\u0001J\"\u0010\u00fe\u0001\u001a\u00020/2\u0006\u0010s\u001a\u00020t2\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0086@\u00a2\u0006\u0003\u0010\u00f5\u0001J#\u0010\u00fe\u0001\u001a\u00020/2\u0007\u0010m\u001a\u00030\u00c0\u00012\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0086@\u00a2\u0006\u0003\u0010\u00c3\u0001J \u0010\u00ff\u0001\u001a\u00020w2\u0007\u0010\u008c\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u0010H\u0082@\u00a2\u0006\u0002\u0010qJ\u0019\u0010\u0080\u0002\u001a\u00020/2\u0007\u0010\u0081\u0002\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0003\u0010\u00b1\u0001J$\u0010\u0082\u0002\u001a\u00020/2\b\u0010\u0083\u0002\u001a\u00030\u00c0\u00012\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0086@\u00a2\u0006\u0003\u0010\u00c3\u0001J\u0019\u0010\u0084\u0002\u001a\u00020/2\u0007\u0010\u00d3\u0001\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0003\u0010\u00b1\u0001J\u0010\u0010\u0085\u0002\u001a\u00020j2\u0007\u0010\u008c\u0001\u001a\u00020\u0010J\u0010\u0010\u0086\u0002\u001a\u00020j2\u0007\u0010\u00f8\u0001\u001a\u000206J\u0019\u0010\u0087\u0002\u001a\u00020/2\u0007\u0010\u00d5\u0001\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0003\u0010\u00b1\u0001J\u000f\u0010\u0088\u0002\u001a\u00020w2\u0006\u0010{\u001a\u00020|J\u0016\u0010\u0089\u0002\u001a\u00020j2\r\u0010\u008a\u0002\u001a\b\u0012\u0004\u0012\u00020\u00120\nJ\u0007\u0010\u008b\u0002\u001a\u00020jJ-\u0010\u008b\u0002\u001a\u00020w2\u0007\u0010\u0087\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u00102\n\b\u0002\u0010m\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0003\u0010\u0088\u0001J\u0007\u0010\u008c\u0002\u001a\u00020jJ\u0010\u0010\u008d\u0002\u001a\u00020wH\u0082@\u00a2\u0006\u0003\u0010\u00bd\u0001J\u0007\u0010\u008e\u0002\u001a\u00020jJ\u0007\u0010\u008f\u0002\u001a\u00020jJ\u0010\u0010\u0090\u0002\u001a\u00020j2\u0007\u0010\u00c7\u0001\u001a\u00020\u0010J#\u0010\u0091\u0002\u001a\u00020j2\u0007\u0010\u00c7\u0001\u001a\u00020\u00102\u0007\u0010m\u001a\u00030\u00c0\u00012\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001J\u0019\u0010\u0092\u0002\u001a\u00020w2\u0007\u0010\u0093\u0002\u001a\u000206H\u0082@\u00a2\u0006\u0003\u0010\u0094\u0002J\u0010\u0010\u0095\u0002\u001a\u00020w2\u0007\u0010\u0096\u0002\u001a\u00020\u0010J\u0010\u0010\u0097\u0002\u001a\u00020w2\u0007\u0010\u0081\u0001\u001a\u00020|J\'\u0010\u0098\u0002\u001a\u00020w2\u0007\u0010\u0087\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u00102\r\u0010\u009a\u0001\u001a\b\u0012\u0004\u0012\u00020\u00100\nJ\'\u0010\u0099\u0002\u001a\u00020w2\u0007\u0010\u008c\u0001\u001a\u00020\u00102\u0006\u0010#\u001a\u00020\u00102\r\u0010\u00c1\u0001\u001a\b\u0012\u0004\u0012\u00020\u00120\nJ\u0010\u0010\u009a\u0002\u001a\u00020j2\u0007\u0010\u009b\u0002\u001a\u00020\u0010J\u0012\u0010\u009c\u0002\u001a\u00020w2\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002J%\u0010\u009d\u0002\u001a\u00020w2\u0007\u0010\u008c\u0001\u001a\u00020\u00102\u000b\b\u0002\u0010\u0096\u0002\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0002\u0010qJ\u0011\u0010\u009e\u0002\u001a\u00020w2\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001J\u0007\u0010\u009f\u0002\u001a\u00020jJ\u0007\u0010\u00a0\u0002\u001a\u00020wJ\u0011\u0010\u00a1\u0002\u001a\u00020w2\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001J\u0007\u0010\u00a2\u0002\u001a\u00020wJ\u0007\u0010\u00a3\u0002\u001a\u00020wJ\t\u0010\u00a4\u0002\u001a\u00020wH\u0002J\u0007\u0010\u00a5\u0002\u001a\u00020wJ\u0010\u0010\u00a6\u0002\u001a\u00020j2\u0007\u0010\u00a7\u0002\u001a\u00020IJ\u0010\u0010\u00a8\u0002\u001a\u00020j2\u0007\u0010\u00af\u0001\u001a\u00020|J&\u0010\u00a9\u0002\u001a\u00020\u00102\u0007\u0010\u00f8\u0001\u001a\u0002062\u000b\b\u0002\u0010\u0096\u0002\u001a\u0004\u0018\u00010\u0010H\u0082@\u00a2\u0006\u0003\u0010\u00aa\u0002J$\u0010\u00ab\u0002\u001a\u00020\u00102\b\u0010\u0083\u0002\u001a\u00030\u00c0\u00012\b\u0010\u00c1\u0001\u001a\u00030\u00c2\u0001H\u0082@\u00a2\u0006\u0003\u0010\u00c3\u0001J\u0019\u0010\u00ac\u0002\u001a\u00020j2\u0007\u0010\u0081\u0001\u001a\u00020|2\u0007\u0010\u00ad\u0002\u001a\u00020\u0010J\u0019\u0010\u00ae\u0002\u001a\u00020j2\u0007\u0010\u0081\u0001\u001a\u00020|2\u0007\u0010\u00af\u0002\u001a\u00020\u0010J\u0017\u0010\u00b0\u0002\u001a\u00020j2\u0006\u0010{\u001a\u00020|2\u0006\u0010k\u001a\u00020\u0010J\u0019\u0010\u00b1\u0002\u001a\u00020j2\u0007\u0010\u0081\u0001\u001a\u00020|2\u0007\u0010\u00c9\u0001\u001a\u00020\u0010J3\u0010\u00b2\u0002\u001a\u00020j2\u0007\u0010\u00af\u0001\u001a\u00020|2\u0006\u0010k\u001a\u00020\u00102\u0007\u0010\u00a1\u0001\u001a\u00020\u00102\u0007\u0010\u00a6\u0001\u001a\u00020\u00102\u0007\u0010\u00a7\u0001\u001a\u00020|J \u0010\u00b3\u0002\u001a\u00020j2\u0007\u0010\u00af\u0001\u001a\u00020|2\u000e\u0010\u00b4\u0002\u001a\t\u0012\u0005\u0012\u00030\u00b5\u00020\nJ\u0019\u0010\u00b6\u0002\u001a\u00020j2\u0007\u0010\u0081\u0001\u001a\u00020|2\u0007\u0010\u00c9\u0001\u001a\u00020\u0010J\u0012\u0010\u00b7\u0002\u001a\u00020w2\u0007\u0010\u008c\u0001\u001a\u00020\u0010H\u0002R\u001a\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010\u000e\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000f0\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00100\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00100\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00160\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00160\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00160\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00160\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00100\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00160\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00160\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u001d\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001e0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u001f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010 0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00100\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010#\u001a\u0010\u0012\f\u0012\n %*\u0004\u0018\u00010\u00100\u00100$\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u001d\u0010(\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0)\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010+R\u001d\u0010,\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\n0)\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010+R\u001f\u0010.\u001a\u0010\u0012\f\u0012\n %*\u0004\u0018\u00010/0/0$\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010\'R)\u00101\u001a\u001a\u0012\u0016\u0012\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100\u000f0\n0)\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u0010+R\u001d\u00103\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\n0)\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010+R\u0010\u00105\u001a\u0004\u0018\u000106X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u00107\u001a\u000208X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u00109\u001a\b\u0012\u0004\u0012\u00020\u00100:X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010;\u001a\b\u0012\u0004\u0012\u00020\u00100)\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010+R\u0017\u0010=\u001a\b\u0012\u0004\u0012\u00020\u00100)\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010+R\u0017\u0010?\u001a\b\u0012\u0004\u0012\u00020\u00160)\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010+R\u000e\u0010@\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010A\u001a\b\u0012\u0004\u0012\u00020\u00160)\u00a2\u0006\b\n\u0000\u001a\u0004\bA\u0010+R\u0017\u0010B\u001a\b\u0012\u0004\u0012\u00020\u00160)\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010+R\u001f\u0010C\u001a\u0010\u0012\f\u0012\n %*\u0004\u0018\u00010\u00160\u00160$\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010\'R\u0017\u0010D\u001a\b\u0012\u0004\u0012\u00020\u00160)\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010+R\u001f\u0010E\u001a\u0010\u0012\f\u0012\n %*\u0004\u0018\u00010\u00160\u00160$\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u0010\'R\u000e\u0010F\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010G\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020I0\n0H\u00a2\u0006\b\n\u0000\u001a\u0004\bJ\u0010KR\u0010\u0010L\u001a\u0004\u0018\u00010MX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010N\u001a\b\u0012\u0004\u0012\u00020\u00100)\u00a2\u0006\b\n\u0000\u001a\u0004\bO\u0010+R\u0010\u0010P\u001a\u0004\u0018\u00010QX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010R\u001a\u0010\u0012\f\u0012\n %*\u0004\u0018\u00010\u00100\u00100$\u00a2\u0006\b\n\u0000\u001a\u0004\bS\u0010\'R\u0017\u0010T\u001a\b\u0012\u0004\u0012\u00020\u00160)\u00a2\u0006\b\n\u0000\u001a\u0004\bU\u0010+R\u0017\u0010V\u001a\b\u0012\u0004\u0012\u00020\u00160)\u00a2\u0006\b\n\u0000\u001a\u0004\bW\u0010+R\u0019\u0010X\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001e0)\u00a2\u0006\b\n\u0000\u001a\u0004\bY\u0010+R\u0019\u0010Z\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010 0)\u00a2\u0006\b\n\u0000\u001a\u0004\b[\u0010+R\u0010\u0010\\\u001a\u0004\u0018\u00010]X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001f\u0010^\u001a\u0010\u0012\f\u0012\n %*\u0004\u0018\u00010\u00100\u00100$\u00a2\u0006\b\n\u0000\u001a\u0004\b_\u0010\'R\u001a\u0010`\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00100aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010b\u001a\u0004\u0018\u00010cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010d\u001a\u00020\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001d\u0010e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\n0)\u00a2\u0006\b\n\u0000\u001a\u0004\bf\u0010+R\u0017\u0010g\u001a\b\u0012\u0004\u0012\u00020\u00100)\u00a2\u0006\b\n\u0000\u001a\u0004\bh\u0010+\u00a8\u0006\u00ba\u0002"}, d2 = {"Lcom/example/app/viewmodel/NoteViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "Landroid/speech/tts/TextToSpeech$OnInitListener;", "repository", "Lcom/example/app/data/NoteRepository;", "app", "Landroid/app/Application;", "(Lcom/example/app/data/NoteRepository;Landroid/app/Application;)V", "_allReminders", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/app/data/Reminder;", "_allTasks", "Lcom/example/app/data/Task;", "_assistantChatHistory", "Lkotlin/Pair;", "", "_chatMessages", "Lcom/example/app/data/ChatMessage;", "_detectedLanguage", "_fullTranscript", "_isAiLoading", "", "_isListening", "_isProcessing", "_isSpeaking", "_preferredLanguage", "_shouldAutoRestart", "_shouldCloseAssistant", "_showListCreationChoice", "Lcom/example/app/viewmodel/NoteViewModel$ListCreationChoice;", "_showTaskReminderChoice", "Lcom/example/app/viewmodel/NoteViewModel$TaskReminderChoice;", "_voiceSessionHistory", "_voiceText", "aiResponse", "Landroidx/lifecycle/MutableLiveData;", "kotlin.jvm.PlatformType", "getAiResponse", "()Landroidx/lifecycle/MutableLiveData;", "allReminders", "Lkotlinx/coroutines/flow/StateFlow;", "getAllReminders", "()Lkotlinx/coroutines/flow/StateFlow;", "allTasks", "getAllTasks", "amplitude", "", "getAmplitude", "assistantChatHistory", "getAssistantChatHistory", "chatMessages", "getChatMessages", "compressedAudioFile", "Ljava/io/File;", "compressedAudioRecorder", "Lcom/example/app/audio/CompressedAudioRecorder;", "currentListItems", "", "detectedLanguage", "getDetectedLanguage", "fullTranscript", "getFullTranscript", "isAiLoading", "isCreatingList", "isListening", "isProcessing", "isRecording", "isSpeaking", "isVoiceOverlayVisible", "listType", "notes", "Landroidx/lifecycle/LiveData;", "Lcom/example/app/data/Note;", "getNotes", "()Landroidx/lifecycle/LiveData;", "openAITTS", "Lcom/example/app/utils/OpenAITTS;", "preferredLanguage", "getPreferredLanguage", "recognizerIntent", "Landroid/content/Intent;", "searchQuery", "getSearchQuery", "shouldAutoRestart", "getShouldAutoRestart", "shouldCloseAssistant", "getShouldCloseAssistant", "showListCreationChoice", "getShowListCreationChoice", "showTaskReminderChoice", "getShowTaskReminderChoice", "speechRecognizer", "Landroid/speech/SpeechRecognizer;", "summary", "getSummary", "supportedLanguages", "", "tts", "Landroid/speech/tts/TextToSpeech;", "ttsReady", "voiceSessionHistory", "getVoiceSessionHistory", "voiceText", "getVoiceText", "addNoteWithBroadcast", "Lkotlinx/coroutines/Job;", "title", "content", "imageUri", "analyzeImageWithOpenAI", "base64Image", "extractedText", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "analyzeImageWithVision", "bitmap", "Landroid/graphics/Bitmap;", "(Landroid/graphics/Bitmap;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "appendTranscript", "", "newText", "isFinal", "archiveNote", "id", "", "askAssistant", "question", "bitmapToBase64", "broadcastUpdate", "noteId", "bodyOverride", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "checkForListCompletion", "userInput", "checkForNoteTaskCreation", "userMessage", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "chooseReminder", "chooseTask", "cleanBracketsFromText", "text", "cleanExtractedText", "cleanOCRForTranscript", "clearAssistantChat", "clearChatHistory", "clearVoiceSession", "clearVoiceText", "completeReminder", "reminderId", "compressAndSendAudioToOpenAI", "copyImageToAppStorage", "contentUri", "createCheckboxNote", "createCheckboxNoteFromList", "items", "createNote", "createNoteFromVoice", "originalText", "createRecognizerIntent", "createRegularNote", "createReminder", "description", "reminderTime", "createReminderFromChat", "createSampleTasks", "createTask", "priority", "dueDate", "createTaskFromChat", "createTaskFromVoiceContext", "taskContent", "createVoiceReminder", "deleteNote", "deleteReminder", "deleteTask", "taskId", "detectLanguage", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "detectListItems", "detectNoteIntent", "detectReminderIntent", "detectTaskIntent", "recentHistory", "detectTaskOrReminderChoice", "dismissListCreationChoice", "dismissTaskReminderChoice", "endVoiceSession", "exportAllData", "Lcom/example/app/data/ExportData;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractDocumentText", "documentUri", "Landroid/net/Uri;", "context", "Landroid/content/Context;", "(Landroid/net/Uri;Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractListItems", "input", "extractMainActionFromMessage", "message", "extractNotesAndTasksWithStructure", "transcript", "extractReminderContent", "extractReminderFromMessage", "extractReminderTime", "extractSummaryAndTasksWithOpenAI", "extractTaskFromConversation", "currentText", "extractTaskFromMessage", "extractTaskFromMessageContent", "extractVideoSummary", "videoUrl", "fetchWebPageContent", "webUrl", "generateFallbackTitle", "generateSmartTitle", "generateSummary", "generateTitle", "getAssistantResponse", "getCurrentTimestamp", "getNoteById", "getRawAudioFilePath", "getSupportedLanguages", "hideVoiceOverlay", "importData", "exportData", "replaceExisting", "(Lcom/example/app/data/ExportData;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "initializeTTS", "isReminderRequest", "isTaskRelated", "isTaskToday", "date", "isToday", "timestamp", "loadChatMessages", "loadRemindersOnce", "loadTasks", "loadTasksOnce", "makeTextMoreNatural", "onCleared", "onInit", "status", "parseTimeFromMessage", "performOCR", "(Landroid/graphics/Bitmap;Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "performOCRRaw", "playAudioFile", "audioFile", "processDocument", "processExtractedContent", "contentType", "processImageContent", "ocrText", "processImageWithOCR", "processMultilingualIntent", "processTextNote", "textContent", "processUploadedAudio", "audioUri", "processVideoUrl", "processVoiceCommand", "processVoiceCommandMultilingual", "processWebPageUrl", "readAloud", "saveChatAsChat", "messages", "saveChatAsNote", "saveChatAsTask", "saveListAsNote", "saveTestChatNote", "saveVoiceSessionAsNote", "sendChatMessage", "sendChatMessageWithImage", "sendCompressedAudioToOpenAI", "file", "(Ljava/io/File;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setPreferredLanguage", "languageCode", "setReminder", "showListCreationChoiceDialog", "showTaskReminderChoiceDialog", "showVoiceOverlay", "query", "speakText", "speakTextInLanguage", "startListening", "startNewChatSession", "startNewVoiceSession", "startSpeechRecognition", "stopAndSaveNote", "stopListening", "stopSpeaking", "stopSpeechRecognition", "toggleFavorite", "note", "toggleTaskComplete", "transcribeAudioWithLanguage", "(Ljava/io/File;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "transcribeUploadedAudio", "updateChecklistState", "checklistState", "updateNoteSnippet", "snippet", "updateNoteTitle", "updateSummaryWithOpenAI", "updateTask", "updateTaskCheckboxItems", "checkboxItems", "Lcom/example/app/data/CheckboxItem;", "updateTranscript", "useAndroidTTS", "ListCreationChoice", "TaskReminderChoice", "app_debug"})
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
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.app.viewmodel.NoteViewModel.TaskReminderChoice> _showTaskReminderChoice = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.app.viewmodel.NoteViewModel.TaskReminderChoice> showTaskReminderChoice = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.app.viewmodel.NoteViewModel.ListCreationChoice> _showListCreationChoice = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.app.viewmodel.NoteViewModel.ListCreationChoice> showListCreationChoice = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _detectedLanguage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> detectedLanguage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _preferredLanguage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> preferredLanguage = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.String> supportedLanguages = null;
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
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.app.data.Reminder>> _allReminders = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.Reminder>> allReminders = null;
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
    
    /**
     * Enhanced extraction function that creates both notes and tasks with detailed structure
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object extractNotesAndTasksWithStructure(@org.jetbrains.annotations.NotNull()
    java.lang.String transcript, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
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
    
    private final java.lang.Object broadcastUpdate(long noteId, java.lang.String bodyOverride, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
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
    public final kotlinx.coroutines.flow.StateFlow<com.example.app.viewmodel.NoteViewModel.TaskReminderChoice> getShowTaskReminderChoice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.app.viewmodel.NoteViewModel.ListCreationChoice> getShowListCreationChoice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getDetectedLanguage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getPreferredLanguage() {
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
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.app.data.Reminder>> getAllReminders() {
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
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job saveChatAsChat(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.ChatMessage> messages) {
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
    
    private final java.lang.String copyImageToAppStorage(java.lang.String contentUri) {
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
    
    private final java.lang.Object checkForNoteTaskCreation(java.lang.String userMessage, java.lang.String aiResponse, java.lang.String imageUri, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object createTaskFromChat(java.lang.String userMessage, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final boolean isReminderRequest(java.lang.String message) {
        return false;
    }
    
    private final java.lang.Object createReminderFromChat(java.lang.String userMessage, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final long parseTimeFromMessage(java.lang.String message) {
        return 0L;
    }
    
    private final java.lang.String extractReminderFromMessage(java.lang.String message) {
        return null;
    }
    
    private final java.lang.String extractTaskFromMessage(java.lang.String message) {
        return null;
    }
    
    private final java.lang.String extractTaskFromMessageContent(java.lang.String message) {
        return null;
    }
    
    private final java.util.List<java.lang.String> detectListItems(java.lang.String userMessage, java.lang.String aiResponse) {
        return null;
    }
    
    private final java.lang.Object saveChatAsNote(java.lang.String userMessage, java.lang.String aiResponse, java.lang.String imageUri, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
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
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job startNewChatSession() {
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
    
    private final boolean detectReminderIntent(java.lang.String text) {
        return false;
    }
    
    private final long extractReminderTime(java.lang.String text) {
        return 0L;
    }
    
    private final java.lang.String extractReminderContent(java.lang.String text) {
        return null;
    }
    
    private final boolean detectTaskOrReminderChoice(java.lang.String text) {
        return false;
    }
    
    private final kotlinx.coroutines.Job createVoiceReminder(java.lang.String text) {
        return null;
    }
    
    public final void showTaskReminderChoiceDialog(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String aiResponse, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.ChatMessage> context) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job chooseTask() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job chooseReminder() {
        return null;
    }
    
    public final void dismissTaskReminderChoice() {
    }
    
    public final void showListCreationChoiceDialog(@org.jetbrains.annotations.NotNull()
    java.lang.String userMessage, @org.jetbrains.annotations.NotNull()
    java.lang.String aiResponse, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> items) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job createCheckboxNote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job createRegularNote() {
        return null;
    }
    
    public final void dismissListCreationChoice() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job createCheckboxNoteFromList(@org.jetbrains.annotations.NotNull()
    java.lang.String userMessage, @org.jetbrains.annotations.NotNull()
    java.lang.String aiResponse, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> items) {
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
    java.lang.String content, @org.jetbrains.annotations.Nullable()
    java.lang.String imageUri) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job addNoteWithBroadcast(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String content, @org.jetbrains.annotations.Nullable()
    java.lang.String imageUri) {
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
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job updateTaskCheckboxItems(long taskId, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.app.data.CheckboxItem> checkboxItems) {
        return null;
    }
    
    private final boolean isToday(long timestamp) {
        return false;
    }
    
    private final java.lang.String getCurrentTimestamp() {
        return null;
    }
    
    private final kotlinx.coroutines.Job loadRemindersOnce() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job createReminder(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String description, long reminderTime) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job completeReminder(long reminderId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job deleteReminder(long reminderId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object exportAllData(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.app.data.ExportData> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object importData(@org.jetbrains.annotations.NotNull()
    com.example.app.data.ExportData exportData, boolean replaceExisting, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void setPreferredLanguage(@org.jetbrains.annotations.NotNull()
    java.lang.String languageCode) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.String> getSupportedLanguages() {
        return null;
    }
    
    private final java.lang.Object detectLanguage(java.lang.String text, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object transcribeAudioWithLanguage(java.io.File audioFile, java.lang.String languageCode, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object speakTextInLanguage(java.lang.String text, java.lang.String languageCode, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void playAudioFile(java.io.File audioFile) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job processVoiceCommandMultilingual(@org.jetbrains.annotations.NotNull()
    java.io.File audioFile) {
        return null;
    }
    
    private final java.lang.Object processMultilingualIntent(java.lang.String text, java.lang.String aiResponse, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00030\u0006\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00030\u0006H\u00c6\u0003J-\u0010\u0010\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00030\u0006H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0015H\u00d6\u0001J\t\u0010\u0016\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00030\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\t\u00a8\u0006\u0017"}, d2 = {"Lcom/example/app/viewmodel/NoteViewModel$ListCreationChoice;", "", "userMessage", "", "aiResponse", "detectedItems", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V", "getAiResponse", "()Ljava/lang/String;", "getDetectedItems", "()Ljava/util/List;", "getUserMessage", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class ListCreationChoice {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String userMessage = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String aiResponse = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> detectedItems = null;
        
        public ListCreationChoice(@org.jetbrains.annotations.NotNull()
        java.lang.String userMessage, @org.jetbrains.annotations.NotNull()
        java.lang.String aiResponse, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> detectedItems) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getUserMessage() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getAiResponse() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getDetectedItems() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.app.viewmodel.NoteViewModel.ListCreationChoice copy(@org.jetbrains.annotations.NotNull()
        java.lang.String userMessage, @org.jetbrains.annotations.NotNull()
        java.lang.String aiResponse, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> detectedItems) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0003J-\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\n\u00a8\u0006\u0018"}, d2 = {"Lcom/example/app/viewmodel/NoteViewModel$TaskReminderChoice;", "", "text", "", "aiResponse", "context", "", "Lcom/example/app/data/ChatMessage;", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V", "getAiResponse", "()Ljava/lang/String;", "getContext", "()Ljava/util/List;", "getText", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class TaskReminderChoice {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String text = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String aiResponse = null;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.example.app.data.ChatMessage> context = null;
        
        public TaskReminderChoice(@org.jetbrains.annotations.NotNull()
        java.lang.String text, @org.jetbrains.annotations.NotNull()
        java.lang.String aiResponse, @org.jetbrains.annotations.NotNull()
        java.util.List<com.example.app.data.ChatMessage> context) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getText() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getAiResponse() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.example.app.data.ChatMessage> getContext() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.example.app.data.ChatMessage> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.app.viewmodel.NoteViewModel.TaskReminderChoice copy(@org.jetbrains.annotations.NotNull()
        java.lang.String text, @org.jetbrains.annotations.NotNull()
        java.lang.String aiResponse, @org.jetbrains.annotations.NotNull()
        java.util.List<com.example.app.data.ChatMessage> context) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}