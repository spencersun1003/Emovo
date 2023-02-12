package org.pytorch.demo.speechrecognition.ui.statistic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ParseException;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.savvi.rangedatepicker.CalendarPickerView;
import com.savvi.rangedatepicker.SubTitle;

import org.pytorch.demo.speechrecognition.R;
import org.pytorch.demo.speechrecognition.Histogram;

public class StatisticFragment extends Fragment {

    private int year = 0;
    private int monthOfYear = 0;
    private int dayOfMonth = 0;
    private int minute = 0;
    private int houre = 0;
    private TextView showDate;
    private TextView showtime;
    private Histogram Histogram;
    private TextView mtvScoreGeneral;
    private TextView mtvScoreComment1;
    private TextView mtvScoreComment2;

    private final int EvalThreshold=50;

    private StatisticViewModel statisticViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        statisticViewModel =
                ViewModelProviders.of(this).get(StatisticViewModel.class);
        View root = inflater.inflate(R.layout.fragment_statistic, container, false);
        //final TextView textView = root.findViewById(R.id.text_notifications);
        statisticViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        mtvScoreGeneral=root.findViewById(R.id.tvScoreGeneral);
        mtvScoreComment1=root.findViewById(R.id.tvScoreComment1);
        mtvScoreComment2=root.findViewById(R.id.tvScoreComment2);
        //initView(root);

        Histogram = root.findViewById(R.id.Histogram);
        final ArrayList<Histogram.ColumnData> dataSource = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Histogram.ColumnData data = Histogram.new ColumnData();
            data.setName("1" + i);
            data.setValue(RandomInt());
            dataSource.add(data);
        }
        Histogram.setDataSource(dataSource);
        Eval(dataSource);
//        Histogram.setColumnOnClickListener(new Histogram.OnColumnClickListener() {
//            @Override
//            public void onColumnClick(View v, int position, Histogram.ColumnData data) {
//                Toast.makeText(root.this, position + "  " + data.toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
        /////////////////////////datepicker///////////////////////////////
        CalendarPickerView calendar;
        ImageButton mImgbtnStatistic;
        final Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 10);

        final Calendar lastYear = Calendar.getInstance();
        lastYear.add(Calendar.YEAR, - 10);

        calendar = root.findViewById(R.id.cvStatiticRange);
        mImgbtnStatistic = root.findViewById(R.id.imgbtnStatitic);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);

        calendar.deactivateDates(list);
        ArrayList<Date> arrayList = new ArrayList<>();
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");

            String strdate = "22-4-2019";
            String strdate2 = "26-4-2019";

            Date newdate = dateformat.parse(strdate);
            Date newdate2 = dateformat.parse(strdate2);
            arrayList.add(newdate);
            arrayList.add(newdate2);
        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }

        calendar.init(lastYear.getTime(), nextYear.getTime(), new SimpleDateFormat("MMMM, YYYY", Locale.getDefault())) //
                .inMode(CalendarPickerView.SelectionMode.RANGE) //
                .withDeactivateDates(list)
                .withSubTitles(getSubTitles())
                .withHighlightedDates(arrayList);

        calendar.scrollToDate(new Date());


        mImgbtnStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getActivity(), "list " + calendar.getSelectedDates().toString(), Toast.LENGTH_LONG).show();
                onDateRangeChanged(calendar.getSelectedDates());

            }
        });
        return root;
    }

//    private void initView(View rootView) {
//        // 日期控件对象
//        DatePicker date = rootView.findViewById(R.id.dpStatistic);
//        // 获得日历对象
//        Calendar c = Calendar.getInstance();
//        // 获取当前年份
//        year = c.get(Calendar.YEAR);
//        // 获取当前月份
//        monthOfYear = c.get(Calendar.MONTH);
//        // 获取当前月份的天数
//        dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
//        // 获取当前的小时数
//        houre = c.get(Calendar.HOUR_OF_DAY);
//        // 获取当前的分钟数
//        minute = c.get(Calendar.MINUTE);
//
//        // 时间显示的文本对象
//        showDate = rootView.findViewById(R.id.tvStatisticTest);
//        hideDateHeaderLayout(date);
//
//
//        // 为日期设置监听事件
//        date.init(year, monthOfYear, dayOfMonth, new OnDateChangedListener() {
//
//            @Override
//            public void onDateChanged(DatePicker view, int year,
//                                      int monthOfYear, int dayOfMonth) {
//                StatisticFragment.this.year = year;
//                StatisticFragment.this.monthOfYear = monthOfYear;
//                StatisticFragment.this.dayOfMonth = dayOfMonth;
//                showDate(year, monthOfYear + 1, dayOfMonth);
//
//            }
//
//        });
//
//
//
//    }

    private void hideDateHeaderLayout(DatePicker picker) {
        final int id = Resources.getSystem().getIdentifier("date_header", "id", "android");
        System.out.println("id=============="+id);
        final View timeLayout = picker.findViewById(id);
        if(timeLayout != null) {
            timeLayout .setVisibility(View.GONE);
        }
    }

    //显示日期的方法
    @SuppressLint("SetTextI18n")
    private void showDate(int year, int monthOfYear, int dayOfMonth) {
        showDate.setText("Date:" + year + "y" + monthOfYear + "m" + dayOfMonth
                + "d");

    }

    private ArrayList<SubTitle> getSubTitles() {
        final ArrayList<SubTitle> subTitles = new ArrayList<>();
        final Calendar tmrw = Calendar.getInstance();
        tmrw.add(Calendar.DAY_OF_MONTH, 1);
        subTitles.add(new SubTitle(tmrw.getTime(), ""));
        return subTitles;
    }

    private int GetDate(Date myDate){
        Calendar cal = Calendar.getInstance();
        cal.setTime(myDate);
        int day= cal.get(Calendar.DAY_OF_MONTH);
        return day;
    }

    private void onDateRangeChanged(List<Date> DateList){
        ArrayList<Histogram.ColumnData> dataSource=new ArrayList<>();
        for (int i = 0; i < DateList.size(); i++) {
            Date date=DateList.get(i);
            Histogram.ColumnData data = Histogram.new ColumnData();
            data.setName(String.valueOf(GetDate(date)));
            data.setValue(RandomInt());
            dataSource.add(data);
        }
        Histogram.setDataSource(dataSource);
        Eval(dataSource);
    }

    private int RandomInt(){
        final double d = Math.random();
        return (int)(d*5);
    }



    @SuppressLint("SetTextI18n")
    private void Eval(ArrayList<Histogram.ColumnData> Dataarr){
        int sum=0;
        int num=Dataarr.size();
        int score=0;
        for (int i=0;i<num;i++){
            sum+=Dataarr.get(i).getValue();
        }

        score=(int)((float)sum/(float)num*20);
        mtvScoreGeneral.setText("Eval: "+score);
        if (score>EvalThreshold){
            //good state
            mtvScoreComment1.setText("Great state");
            mtvScoreComment2.setText("Adjust mental attitude\nKeep it up!");
        }
        else {
            mtvScoreComment1.setText("Take care");
            mtvScoreComment2.setText("Adjust mental attitude\nYou can make it!");
        }
    }


}