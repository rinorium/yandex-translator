package com.github.linolium.yandex_translator.ui.main.dictionary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.github.linolium.yandex_translator.R;
import com.github.linolium.yandex_translator.app.BaseFragment;
import com.github.linolium.yandex_translator.common.MessageType;
import com.github.linolium.yandex_translator.common.adapters.SavedTextAdapter;
import com.github.linolium.yandex_translator.di.components.MainComponent;
import com.github.linolium.yandex_translator.domain.TranslateText;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import rx.Subscription;

/**
 * Created by Linolium on 11.04.2017.
 */

public class DictionaryFragment extends BaseFragment implements DictionaryFragmentView {

    private ProgressBar progressBar;
    private Subscription eventSubscription;
    private Activity activity;
    private RecyclerView recyclerView;
    private SavedTextAdapter savedTextAdapter;
    private SearchView searchView;


    @Inject
    DictionaryFragmentPresenter presenter;
    Realm realm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = context instanceof Activity ? (Activity) context : null;
    }

    @Override
    public void onResume() {
        super.onResume();
        realm = Realm.getDefaultInstance();
        presenter.init(this);
        eventSubscription = presenter.subscribeToBus(bus, preferences, realm);
        presenter.getSavedTexts(bus, realm);
    }

    @Override
    public void onPause() {
        if (eventSubscription != null && !eventSubscription.isUnsubscribed())
            eventSubscription.unsubscribe();
        super.onPause();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getComponent(MainComponent.class).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        searchView = (SearchView) view.findViewById(R.id.searchView);
        recyclerView = (RecyclerView) view.findViewById(R.id.rvSavedTexts);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                savedTextAdapter.filterList(s);
                return false;
            }
        });
        return view;
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showMessage(int message, @MessageType int type) {
        showToast(message, type);
    }

    @Override
    public void getTranslatedTexts(List<TranslateText> translateTexts) {
        savedTextAdapter = new SavedTextAdapter(translateTexts, activity, bus);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(savedTextAdapter);
    }
}