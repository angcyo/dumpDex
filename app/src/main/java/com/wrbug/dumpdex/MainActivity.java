package com.wrbug.dumpdex;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 20);

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 20 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadData();
        }
    }

    private void loadData() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        final List<Bean> datas = new ArrayList<>();
        final List<Bean> noDatas = new ArrayList<>();
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(RLog.getLogFilePath()));

            String readLine = reader.readLine();

            Bean lastBean = null;
            while (readLine != null) {
                Log.w("angcyo", readLine);
                if (readLine.contains("Find")) {
                    String[] split = readLine.split(":");
                    if (split.length > 2) {
                        Bean bean = new Bean();
                        bean.packageName = split[1];
                        bean.jm = split[2];

                        if (!"null".equals(bean.jm)) {
                            lastBean = bean;
                            if (!datas.contains(bean)) {
                                datas.add(bean);
                            }
                        } else {
                            lastBean = null;
                            if (!noDatas.contains(bean)) {
                                noDatas.add(bean);
                            }
                        }
                    } else {
                        lastBean = null;
                    }
                } else if (readLine.contains("Hook")) {
                    String[] split = readLine.split(":");
                    if (lastBean != null && split.length > 2) {
                        lastBean.path = split[2];
                    }
                }

                readLine = reader.readLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        datas.addAll(noDatas);

        recyclerView.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MyViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_layout, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
                Bean bean = datas.get(position);
                holder.tv(R.id.text1).setText(bean.packageName);
                holder.tv(R.id.text2).setText(bean.jm);

                if (!TextUtils.isEmpty(bean.path)) {
                    File file = new File(bean.path);
                    if (file.exists() && file.isDirectory()) {
                        String[] list = file.list();
                        if (list != null) {
                            holder.tv(R.id.text3).setText(bean.path + " " + list.length);
                        } else {
                            holder.tv(R.id.text3).setText(bean.path + " -");
                        }
                    } else {
                        holder.tv(R.id.text3).setText(bean.path);
                    }
                }
            }

            @Override
            public int getItemCount() {
                return datas.size();
            }
        });
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
        }

        public TextView tv(int id) {
            return itemView.findViewById(id);
        }
    }

    static class Bean {
        String packageName;
        String jm;
        String path;

        @Override
        public boolean equals(Object obj) {
            return TextUtils.equals(packageName, ((Bean) obj).packageName);
        }
    }
}
