package com.kyad.traystorage.app.chatbot;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kyad.traystorage.R;
import com.kyad.traystorage.data.DataManager;
import com.kyad.traystorage.data.model.ModelChatMessage;
import com.kyad.traystorage.data.model.ModelUser;
import com.kyad.traystorage.databinding.FragmentChatbotBinding;
import com.kyad.traystorage.databinding.ItemChatMessageBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatbotFragment extends Fragment {

    private FragmentChatbotBinding binding;
    private ChatMessageAdapter adapter;
    private List<ModelChatMessage> messages = new ArrayList<>();
    private ObservableBoolean isExpanded = new ObservableBoolean(false);
    private ChatbotService chatbotService;
    private OnChatbotCloseListener closeListener;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;

    public interface OnChatbotCloseListener {
        void onChatbotClose();
    }

    public void setOnCloseListener(OnChatbotCloseListener listener) {
        this.closeListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatbot, container, false);
        binding.setFragment(this);
        binding.setIsExpanded(isExpanded.get());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initChatbot();
        setupRecyclerView();
        setupInputListener();
        setupKeyboardListener();
        addWelcomeMessage();
    }

    private void setupKeyboardListener() {
        // 키보드 처리는 Activity에서 chatbot_container를 직접 제어
    }

    private void initChatbot() {
        chatbotService = new ChatbotService();
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.chatList.setLayoutManager(layoutManager);
        binding.chatList.setAdapter(adapter);
    }

    private void setupInputListener() {
        binding.inputMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                onSendClick();
                return true;
            }
            return false;
        });
    }

    private void addWelcomeMessage() {
        ModelChatMessage welcomeMessage = new ModelChatMessage(
                System.currentTimeMillis() + "-assistant",
                "assistant",
                "안녕하세요! 저는 TrayStorage의 AI 어시스턴트 트로이입니다. 😊 문서 검색과 관리를 도와드릴게요!",
                new Date()
        );
        messages.add(welcomeMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    public void onExpandClick() {
        isExpanded.set(!isExpanded.get());
        binding.setIsExpanded(isExpanded.get());
        binding.executePendingBindings();
        
        ViewGroup.LayoutParams params = binding.chatList.getLayoutParams();
        params.height = isExpanded.get() ? 
                getResources().getDimensionPixelSize(R.dimen.chat_height_expanded) : 
                getResources().getDimensionPixelSize(R.dimen.chat_height_default);
        binding.chatList.setLayoutParams(params);
    }

    public void onCloseClick() {
        if (closeListener != null) {
            closeListener.onChatbotClose();
        }
    }

    public void onSendClick() {
        String inputText = binding.inputMessage.getText().toString().trim();
        if (inputText.isEmpty()) return;

        binding.inputMessage.setText("");
        sendMessage(inputText);
    }

    public void onQuickQuestion1() {
        sendMessage("문서 검색 방법을 알려줘");
    }

    public void onQuickQuestion2() {
        sendMessage("전체 문서 수는?");
    }

    public void onQuickQuestion3() {
        sendMessage("최근 문서 보여줘");
    }

    private void sendMessage(String content) {
        ModelChatMessage userMessage = new ModelChatMessage(
                System.currentTimeMillis() + "-user",
                "user",
                content,
                new Date()
        );
        messages.add(userMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        binding.quickQuestionsLayout.setVisibility(View.GONE);

        String assistantId = System.currentTimeMillis() + "-assistant";
        ModelChatMessage assistantMessage = new ModelChatMessage(
                assistantId,
                "assistant",
                "",
                new Date()
        );
        messages.add(assistantMessage);
        int assistantIndex = messages.size() - 1;
        adapter.notifyItemInserted(assistantIndex);

        ModelUser user = DataManager.get().getModel(ModelUser.class);
        String userId = user != null ? String.valueOf(user.id) : "anonymous";
        String accessToken = user != null ? user.access_token : "";

        List<ModelChatMessage> history = new ArrayList<>(messages.subList(0, messages.size() - 1));

        chatbotService.sendMessage(content, userId, accessToken, history, new ChatbotService.ChatbotCallback() {
            @Override
            public void onStreamUpdate(String text) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    messages.get(assistantIndex).content = text;
                    adapter.notifyItemChanged(assistantIndex);
                    scrollToBottom();
                });
            }

            @Override
            public void onComplete(String fullText, List<ModelChatMessage.SearchResult> searchResults) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    messages.get(assistantIndex).content = fullText;
                    messages.get(assistantIndex).searchResults = searchResults;
                    adapter.notifyItemChanged(assistantIndex);
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    messages.get(assistantIndex).content = "죄송합니다. 오류가 발생했습니다: " + error;
                    adapter.notifyItemChanged(assistantIndex);
                    scrollToBottom();
                });
            }
        });
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            binding.chatList.smoothScrollToPosition(messages.size() - 1);
        }
    }

    private class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemChatMessageBinding itemBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.item_chat_message,
                    parent,
                    false
            );
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(messages.get(position));
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final ItemChatMessageBinding itemBinding;

            ViewHolder(ItemChatMessageBinding binding) {
                super(binding.getRoot());
                this.itemBinding = binding;
            }

            void bind(ModelChatMessage message) {
                itemBinding.setMessage(message);
                
                SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREAN);
                String timeStr = sdf.format(message.timestamp);

                if ("user".equals(message.role)) {
                    itemBinding.userTimestamp.setText(timeStr);
                    itemBinding.userMessageText.setText(parseBoldText(message.content));
                } else {
                    itemBinding.assistantTimestamp.setText(timeStr);
                    itemBinding.assistantMessageText.setText(parseBoldText(message.content));

                    if (message.searchResults != null && !message.searchResults.isEmpty()) {
                        itemBinding.searchResultsLayout.setVisibility(View.VISIBLE);
                        itemBinding.searchResultsContainer.removeAllViews();
                        
                        for (ModelChatMessage.SearchResult result : message.searchResults) {
                            TextView resultView = new TextView(getContext());
                            resultView.setText("• " + result.name + (result.storageLocation != null ? " (" + result.storageLocation + ")" : ""));
                            resultView.setTextSize(12);
                            resultView.setTextColor(getResources().getColor(R.color.C555555));
                            itemBinding.searchResultsContainer.addView(resultView);
                        }
                    } else {
                        itemBinding.searchResultsLayout.setVisibility(View.GONE);
                    }
                }

                itemBinding.executePendingBindings();
            }
        }
    }

    private SpannableString parseBoldText(String text) {
        if (text == null) return new SpannableString("");
        
        SpannableString spannable = new SpannableString(text.replaceAll("\\*\\*", ""));
        Pattern pattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
        Matcher matcher = pattern.matcher(text);

        int offset = 0;
        while (matcher.find()) {
            int start = matcher.start() - offset;
            int end = start + matcher.group(1).length();
            offset += 4;
            
            if (start >= 0 && end <= spannable.length()) {
                spannable.setSpan(
                        new StyleSpan(android.graphics.Typeface.BOLD),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        return spannable;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatbotService != null) {
            chatbotService.cancel();
        }
        // 키보드 리스너 제거
        if (keyboardListener != null && binding != null) {
            binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
        }
        binding = null;
    }
}
