package base;

import java.util.ArrayList;
import java.util.List;

public class BaseEvent {
    private BaseEvent() {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    public static class LoadingEvent {
        public boolean isLoading = false;
        public LoadingEvent(boolean isLoading) {
            this.isLoading = isLoading;
        }
    }

    public static class PhotoSelectCompleteEvent {
        public List<String> photoUrlList = new ArrayList<>();
        public PhotoSelectCompleteEvent(List<String> photoUrlList) {
            this.photoUrlList = photoUrlList;
        }
    }
}
