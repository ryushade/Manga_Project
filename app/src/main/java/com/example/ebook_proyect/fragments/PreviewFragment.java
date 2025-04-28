package com.example.ebook_proyect.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ebook_proyect.databinding.FragmentPreviewBinding;

public class PreviewFragment extends Fragment {

    private FragmentPreviewBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPreviewBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        WebView webView = binding.webView;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Obtén el ID del libro desde los argumentos
        if (getArguments() != null) {
            String bookId = getArguments().getString("BOOK_ID");
            loadBookPreview(bookId);
        }

        return view;
    }

    private void loadBookPreview(String bookId) {
        String webReaderLink = "https://www.pdfbooksworld.com/bibi/pre.html?book=" + bookId + ".epub";
        binding.webView.loadUrl(webReaderLink);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear the binding reference to avoid memory leaks
    }
}