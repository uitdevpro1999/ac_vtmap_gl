import android.app.Activity;
import android.os.Bundle;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.concurrent.atomic.AtomicInteger;

public class MapboxMapsPlugin implements FlutterPlugin, ActivityAware, Application.ActivityLifecycleCallbacks {
  // State constants
  static final int CREATED = 1;
  static final int STARTED = 2;
  static final int RESUMED = 3;
  static final int PAUSED = 4;
  static final int STOPPED = 5;
  static final int DESTROYED = 6;

  private final AtomicInteger state = new AtomicInteger(0);
  private MethodChannel methodChannel;
  private ActivityPluginBinding activityBinding;

  // For API v1
  public static void registerWith(Registrar registrar) {
    MapboxMapsPlugin plugin = new MapboxMapsPlugin(registrar);
    registrar.activity().getApplication().registerActivityLifecycleCallbacks(plugin);
    plugin.methodChannel = new MethodChannel(registrar.messenger(), "plugins.flutter.io/mapbox_gl");
    plugin.methodChannel.setMethodCallHandler(new GlobalMethodHandler());
    registrar.platformViewRegistry().registerViewFactory("plugins.flutter.io/mapbox_gl", new MapboxMapFactory(plugin.state, registrar));
  }

  @Override
  public void onAttachedToEngine(FlutterPlugin.FlutterPluginBinding binding) {
    methodChannel = new MethodChannel(binding.getBinaryMessenger(), "plugins.flutter.io/mapbox_gl");
    methodChannel.setMethodCallHandler(new GlobalMethodHandler());
    binding.getPlatformViewRegistry().registerViewFactory("plugins.flutter.io/mapbox_gl", new MapboxMapFactory(state, null));
  }

  @Override
  public void onDetachedFromEngine(FlutterPlugin.FlutterPluginBinding binding) {
    methodChannel.setMethodCallHandler(null);
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    this.activityBinding = binding;
    binding.addActivityResultListener(new GlobalMethodHandler()); // Đảm bảo thêm listener nếu cần
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    // Handle configuration changes if necessary
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    this.activityBinding = binding;
  }

  @Override
  public void onDetachedFromActivity() {
    activityBinding = null;
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    state.set(CREATED);
  }

  @Override
  public void onActivityStarted(Activity activity) {
    state.set(STARTED);
  }

  @Override
  public void onActivityResumed(Activity activity) {
    if (activity.hashCode() != registrarActivityHashCode) {
      return;
    }
    state.set(RESUMED);
  }

  @Override
  public void onActivityPaused(Activity activity) {
    if (activity.hashCode() != registrarActivityHashCode) {
      return;
    }
    state.set(PAUSED);
  }

  @Override
  public void onActivityStopped(Activity activity) {
    if (activity.hashCode() != registrarActivityHashCode) {
      return;
    }
    state.set(STOPPED);
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

  @Override
  public void onActivityDestroyed(Activity activity) {
    if (activity.hashCode() != registrarActivityHashCode) {
      return;
    }
    state.set(DESTROYED);
  }

  private MapboxMapsPlugin(Registrar registrar) {
    this.registrarActivityHashCode = registrar.activity().hashCode();
  }
}