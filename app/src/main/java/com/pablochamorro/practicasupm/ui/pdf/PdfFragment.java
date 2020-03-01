package com.pablochamorro.practicasupm.ui.pdf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.barteksc.pdfviewer.PDFView;
import com.pablochamorro.practicasupm.R;

import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URL;

public class PdfFragment extends Fragment {

    private PDFView pdfView;
    private String pdf;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pdf, container, false);

        //Obtain data from other fragments
        Bundle args = getArguments();

        pdf = args.getString("pdf");

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pdfView = getActivity().findViewById(R.id.pdfView);

        if (pdf != null){

            //Transform String to bytes
            try {

                byte [] bytes_pdf = pdf.getBytes("ISO-8859-1");

                //Show pdf
                pdfView.fromBytes(bytes_pdf).load();

            }
            catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }

        //pdfView.fromAsset("practica_administracion.pdf").load();
    }
}
