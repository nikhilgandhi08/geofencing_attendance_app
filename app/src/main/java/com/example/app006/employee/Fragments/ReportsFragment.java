package com.example.app006.employee.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app006.R;
import com.example.app006.adapters.InsightAdapter;
import com.example.app006.models.Insight;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;


public class ReportsFragment extends Fragment {

    private TextView tvEmployeeName, tvDesignationDepartment, tvReportPeriod, tvAttendancePercentage;
    private ProgressBar pbAttendance;
    private FirebaseFirestore db;
    private String employeeEmail;
    private BarChart barChart;
    private ScatterChart scatterChart;

    private MaterialCalendarView calendarView;

    private RecyclerView rvInsights;




    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.employee_fragment_reports, container, false);

        // 1️⃣ Initialize views
        tvEmployeeName = view.findViewById(R.id.tv_employee_name);
        tvDesignationDepartment = view.findViewById(R.id.tv_designation_department);
        tvReportPeriod = view.findViewById(R.id.tv_report_period);
        tvAttendancePercentage = view.findViewById(R.id.tv_attendance_percentage); // make sure this id is present
        pbAttendance = view.findViewById(R.id.pb_attendance);
        barChart = view.findViewById(R.id.bar_chart_work_hours);
        scatterChart = view.findViewById(R.id.scatter_chart_login_logout);
        calendarView = view.findViewById(R.id.cv_attendance_calendar);
        rvInsights = view.findViewById(R.id.rv_insights);
        rvInsights.setLayoutManager(new LinearLayoutManager(requireContext()));



        db = FirebaseFirestore.getInstance();

        // 2️⃣ Load from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", null);
        String role = sharedPreferences.getString("role", null);
        employeeEmail = sharedPreferences.getString("email", null);

        if (name == null || role == null || employeeEmail == null) {
            Toast.makeText(requireContext(), "Unable to load user details", Toast.LENGTH_SHORT).show();
            Log.d("ReportsFragment", "Missing SharedPrefs: " + name + ", " + role + ", " + employeeEmail);
        } else {
            tvEmployeeName.setText(name);
            tvDesignationDepartment.setText(role);
        }

        // 3️⃣ Report period display
        setReportPeriod();

        // 4️⃣ Attendance Percentage logic
        calculateAttendancePercentage();

        calculateAverageWorkHours();

        calculateLeavesTaken();

        loadLastFiveDaysWorkHours();

        loadLoginLogoutPattern();

        setupCalendarView();

        loadInsightsDataForCurrentMonth();



        return view;
    }

    private void setReportPeriod() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        String start = displayFormat.format(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String end = displayFormat.format(cal.getTime());

        tvReportPeriod.setText(start + " - " + end);
    }

    private void calculateAttendancePercentage() {
        // Step 1: Get adminEmail from hr-employees
        db.collection("hr-employees")
                .whereEqualTo("empEmail", employeeEmail)
                .get()
                .addOnSuccessListener(hrDocs -> {
                    if (!hrDocs.isEmpty()) {
                        String adminEmail = hrDocs.getDocuments().get(0).getString("adminEmail");

                        // Step 2: Get attendance data for current month
                        db.collection("attendance-data")
                                .whereEqualTo("email", employeeEmail)
                                .get()
                                .addOnSuccessListener(attendanceDocs -> {
                                    Set<String> presentDates = new HashSet<>();
                                    for (QueryDocumentSnapshot doc : attendanceDocs) {
                                        String date = doc.getString("date");
                                        String type = doc.getString("type");
                                        if (date != null && "login".equals(type)) {
                                            presentDates.add(date);
                                        }
                                    }

                                    // Step 3: Get approved leaves
                                    db.collection("leave-requests")
                                            .whereEqualTo("employeeEmail", employeeEmail)
                                            .whereEqualTo("status", "approved")
                                            .get()
                                            .addOnSuccessListener(leaveDocs -> {
                                                Set<String> leaveDates = new HashSet<>();
                                                for (QueryDocumentSnapshot doc : leaveDocs) {
                                                    String start = doc.getString("startDate");
                                                    String end = doc.getString("endDate");
                                                    if (start != null && end != null) {
                                                        leaveDates.addAll(generateDateRange(start, end));
                                                    }
                                                }

                                                // Step 4: Calculate working days this month
                                                List<String> workingDays = getWorkingDaysInCurrentMonth();
                                                workingDays.removeAll(leaveDates);

                                                int totalWorkingDays = workingDays.size();
                                                int presentDays = 0;

                                                for (String date : workingDays) {
                                                    if (presentDates.contains(date)) {
                                                        presentDays++;
                                                    }
                                                }

                                                int percentage = (totalWorkingDays > 0) ?
                                                        (presentDays * 100 / totalWorkingDays) : 0;

                                                updateAttendanceUI(percentage);
                                            });
                                });
                    }
                });
    }

    private List<String> generateDateRange(String startDate, String endDate) {
        List<String> dates = new ArrayList<>();
        try {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            start.setTime(sdf.parse(startDate));
            end.setTime(sdf.parse(endDate));

            while (!start.after(end)) {
                dates.add(sdf.format(start.getTime()));
                start.add(Calendar.DATE, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dates;
    }

    private List<String> getWorkingDaysInCurrentMonth() {
        List<String> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        cal.set(year, month, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= maxDay; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                dates.add(sdf.format(cal.getTime()));
            }
        }
        return dates;
    }

    private void updateAttendanceUI(int percentage) {
        tvAttendancePercentage.setText(percentage + "%");
        pbAttendance.setProgress(percentage);
    }

    private void calculateAverageWorkHours() {
        db.collection("attendance-data")
                .whereEqualTo("email", employeeEmail)
                .get()
                .addOnSuccessListener(docs -> {
                    Map<String, String> loginMap = new HashMap<>();
                    Map<String, String> logoutMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : docs) {
                        String date = doc.getString("date");
                        String type = doc.getString("type");
                        String time = doc.getString("time");

                        if (date != null && time != null) {
                            if ("login".equals(type)) {
                                // Store earliest login (or replace if earlier)
                                loginMap.putIfAbsent(date, time);
                            } else if ("logout".equals(type)) {
                                // Store latest logout (or replace if later)
                                logoutMap.put(date, time);
                            }
                        }
                    }

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    long totalMinutes = 0;
                    int validDays = 0;

                    for (String date : loginMap.keySet()) {
                        if (logoutMap.containsKey(date)) {
                            try {
                                Date loginTime = timeFormat.parse(loginMap.get(date));
                                Date logoutTime = timeFormat.parse(logoutMap.get(date));
                                if (loginTime != null && logoutTime != null && logoutTime.after(loginTime)) {
                                    long diffMillis = logoutTime.getTime() - loginTime.getTime();
                                    totalMinutes += diffMillis / (60 * 1000);
                                    validDays++;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (validDays > 0) {
                        long avgMinutes = totalMinutes / validDays;
                        long hours = avgMinutes / 60;
                        long minutes = avgMinutes % 60;
                        updateAvgWorkHoursUI(String.format(Locale.getDefault(), "%dh %02dm", hours, minutes));
                    } else {
                        updateAvgWorkHoursUI("0h 00m");
                    }
                });
    }

    private void updateAvgWorkHoursUI(String avgTime) {
        TextView tvAvgHours = requireView().findViewById(R.id.tv_avg_work_hours);
        tvAvgHours.setText(avgTime);
    }


    private void calculateLeavesTaken() {
        db.collection("leave-requests")
                .whereEqualTo("employeeEmail", employeeEmail)
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(docs -> {
                    Set<String> monthlyLeaveDates = new HashSet<>();

                    Calendar now = Calendar.getInstance();
                    int currentMonth = now.get(Calendar.MONTH);
                    int currentYear = now.get(Calendar.YEAR);

                    for (QueryDocumentSnapshot doc : docs) {
                        String start = doc.getString("startDate");
                        String end = doc.getString("endDate");

                        if (start != null && end != null) {
                            List<String> dateRange = generateDateRange(start, end);
                            for (String dateStr : dateRange) {
                                try {
                                    Date date = sdf.parse(dateStr);
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(date);
                                    int month = cal.get(Calendar.MONTH);
                                    int year = cal.get(Calendar.YEAR);
                                    if (month == currentMonth && year == currentYear) {
                                        monthlyLeaveDates.add(dateStr);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    updateLeavesTakenUI(monthlyLeaveDates.size());
                });
    }

    private void updateLeavesTakenUI(int totalLeaveDays) {
        TextView tvLeaves = requireView().findViewById(R.id.tv_leaves_taken);
        tvLeaves.setText(String.valueOf(totalLeaveDays));
    }

    private void loadLastFiveDaysWorkHours() {
        db.collection("attendance-data")
                .whereEqualTo("email", employeeEmail)
                .get()
                .addOnSuccessListener(docs -> {
                    Map<String, String> loginMap = new HashMap<>();
                    Map<String, String> logoutMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : docs) {
                        String date = doc.getString("date");
                        String type = doc.getString("type");
                        String time = doc.getString("time");

                        if (date != null && time != null) {
                            if ("login".equals(type)) {
                                loginMap.putIfAbsent(date, time);
                            } else if ("logout".equals(type)) {
                                logoutMap.put(date, time);
                            }
                        }
                    }

                    List<String> sortedDates = new ArrayList<>(loginMap.keySet());
                    Collections.sort(sortedDates); // oldest → newest

                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    List<BarEntry> entries = new ArrayList<>();
                    List<String> xLabels = new ArrayList<>();
                    int index = 0;

                    // Show last 5 days (from end)
                    for (int i = sortedDates.size() - 1; i >= 0 && xLabels.size() < 5; i--) {
                        String date = sortedDates.get(i);
                        if (logoutMap.containsKey(date)) {
                            try {
                                Date in = timeFormat.parse(loginMap.get(date));
                                Date out = timeFormat.parse(logoutMap.get(date));
                                if (in != null && out != null && out.after(in)) {
                                    long diffMillis = out.getTime() - in.getTime();
                                    float hours = diffMillis / (1000f * 60f * 60f);
                                    entries.add(0, new BarEntry(index++, hours));
                                    xLabels.add(0, date.substring(date.lastIndexOf("-") + 1)); // day only
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    BarDataSet dataSet = new BarDataSet(entries, "Work Hours");
                    dataSet.setColor(getResources().getColor(R.color.primary_blue));
                    dataSet.setValueTextSize(10f);

                    BarData barData = new BarData(dataSet);
                    barChart.setData(barData);
                    barChart.getDescription().setEnabled(false);
                    barChart.getLegend().setEnabled(false);

                    XAxis xAxis = barChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
                    xAxis.setGranularity(1f);
                    xAxis.setDrawGridLines(false);
                    //xAxis.setLabelRotationAngle(-45);
                    barData.setBarWidth(0.2f); // 0.4 works well for 5 bars


                    barChart.getAxisLeft().setAxisMinimum(0f);
                    barChart.getAxisRight().setEnabled(false);

                    barChart.invalidate(); // refresh
                });
    }

    private void loadLoginLogoutPattern() {
        db.collection("attendance-data")
                .whereEqualTo("email", employeeEmail)
                .get()
                .addOnSuccessListener(docs -> {
                    Map<String, String> loginMap = new HashMap<>();
                    Map<String, String> logoutMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : docs) {
                        String date = doc.getString("date");
                        String type = doc.getString("type");
                        String time = doc.getString("time");

                        if (date != null && time != null) {
                            if ("login".equals(type)) loginMap.putIfAbsent(date, time);
                            else if ("logout".equals(type)) logoutMap.put(date, time);
                        }
                    }

                    Set<String> allDatesSet = new HashSet<>();
                    allDatesSet.addAll(loginMap.keySet());
                    allDatesSet.retainAll(logoutMap.keySet()); // Only dates with both login & logout

                    List<String> allDates = new ArrayList<>(allDatesSet);
                    Collections.sort(allDates); // oldest to newest

                    // Keep only last 5
                    int start = Math.max(0, allDates.size() - 5);
                    List<String> lastFiveDates = allDates.subList(start, allDates.size());


                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    List<Entry> loginEntries = new ArrayList<>();
                    List<Entry> logoutEntries = new ArrayList<>();
                    List<String> xLabels = new ArrayList<>();

                    int index = 0;
                    for (String date : lastFiveDates) {
                        String loginTime = loginMap.get(date);
                        String logoutTime = logoutMap.get(date);
                        xLabels.add(0, date.substring(date.lastIndexOf("-") + 1)); // day only

                        try {
                            if (loginTime != null) {
                                Date in = timeFormat.parse(loginTime);
                                float hours = in.getHours() + in.getMinutes() / 60f;
                                loginEntries.add(new Entry(index, hours));
                            }
                            if (logoutTime != null) {
                                Date out = timeFormat.parse(logoutTime);
                                float hours = out.getHours() + out.getMinutes() / 60f;
                                logoutEntries.add(new Entry(index, hours));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        index++;
                    }

                    ScatterDataSet loginSet = new ScatterDataSet(loginEntries, "Login Time     ");
                    loginSet.setColor(Color.BLUE);
                    loginSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
                    loginSet.setScatterShapeSize(10f);

                    ScatterDataSet logoutSet = new ScatterDataSet(logoutEntries, "Logout Time");
                    logoutSet.setColor(Color.RED);
                    logoutSet.setScatterShape(ScatterChart.ScatterShape.TRIANGLE);
                    logoutSet.setScatterShapeSize(10f);

                    ScatterData data = new ScatterData(loginSet, logoutSet);
                    scatterChart.setData(data);
                    scatterChart.getDescription().setEnabled(false);
                    scatterChart.getLegend().setTextSize(12f);

                    XAxis xAxis = scatterChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
                    xAxis.setGranularity(1f);
                    xAxis.setDrawGridLines(false);

                    YAxis yAxis = scatterChart.getAxisLeft();
                    yAxis.setAxisMinimum(9f);    // 9 AM
                    yAxis.setAxisMaximum(19f);   // 7 PM
                    yAxis.setGranularity(2f);    // Step every 2 hours
                    yAxis.setLabelCount(6, true); // Ensures even spacing

                    // Format into 12-hour AM/PM format
                    yAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int hour = (int) value;
                            String suffix = (hour >= 12) ? "PM" : "AM";
                            int hour12 = (hour == 0 || hour == 12) ? 12 : hour % 12;
                            return String.format(Locale.getDefault(), "%02d %s", hour12, suffix);
                        }
                    });


                    scatterChart.getAxisRight().setEnabled(false);

                    scatterChart.invalidate(); // Refresh chart
                });
    }


    private void setupCalendarView() {
        calendarView.setCurrentDate(CalendarDay.today());
        calendarView.setSelectedDate(CalendarDay.today());

        loadAttendanceDataForCalendar();

        calendarView.setOnMonthChangedListener((widget, date) -> {
            loadAttendanceDataForMonth(date.getYear(), date.getMonth());
        });
    }

    private void loadAttendanceDataForCalendar() {
        CalendarDay today = CalendarDay.today();
        loadAttendanceDataForMonth(today.getYear(), today.getMonth());
    }

    private void loadAttendanceDataForMonth(int year, int month) {
        db.collection("attendance-data")
                .whereEqualTo("email", employeeEmail)
                .get()
                .addOnSuccessListener(attendanceDocs -> {
                    Set<CalendarDay> presentDays = new HashSet<>();
                    Set<CalendarDay> absentDays = new HashSet<>();
                    Set<CalendarDay> leaveDays = new HashSet<>();

                    Map<String, Boolean> attendanceMap = new HashMap<>();
                    for (QueryDocumentSnapshot doc : attendanceDocs) {
                        String date = doc.getString("date");
                        Boolean inside = doc.getBoolean("insideGeofence");
                        if (date != null && Boolean.TRUE.equals(inside)) {
                            attendanceMap.put(date, true);
                        }
                    }

                    db.collection("leave-requests")
                            .whereEqualTo("employeeEmail", employeeEmail)
                            .whereEqualTo("status", "approved")
                            .get()
                            .addOnSuccessListener(leaveDocs -> {
                                Set<String> leaveDates = new HashSet<>();
                                for (QueryDocumentSnapshot doc : leaveDocs) {
                                    String start = doc.getString("startDate");
                                    String end = doc.getString("endDate");
                                    if (start != null && end != null) {
                                        leaveDates.addAll(generateDateRange(start, end));
                                    }
                                }

                                List<String> workingDays = getWorkingDaysForMonth(year, month);

                                for (String dateStr : workingDays) {
                                    try {
                                        Date date = sdf.parse(dateStr);
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(date);
                                        CalendarDay calendarDay = CalendarDay.from(cal);

                                        if (leaveDates.contains(dateStr)) {
                                            leaveDays.add(calendarDay);
                                        } else if (Boolean.TRUE.equals(attendanceMap.get(dateStr))) {
                                            presentDays.add(calendarDay);
                                        } else {
                                            Calendar today = Calendar.getInstance();
                                            if (!cal.after(today)) {
                                                absentDays.add(calendarDay);
                                            }
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }

                                applyCalendarDecorators(presentDays, absentDays, leaveDays);
                            });
                });
    }

    private List<String> getWorkingDaysForMonth(int year, int month) {
        List<String> dates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1); // month is 0-based, no need to subtract

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                dates.add(sdf.format(cal.getTime()));
            }
        }
        return dates;
    }

    private void applyCalendarDecorators(Set<CalendarDay> presentDays,
                                         Set<CalendarDay> absentDays,
                                         Set<CalendarDay> leaveDays) {

        calendarView.removeDecorators();

        // Present
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return presentDays.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_present_day));
            }
        });

// Absent
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return absentDays.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_absent_day));
            }
        });

// Leave
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay day) {
                return leaveDays.contains(day);
            }

            @Override
            public void decorate(DayViewFacade view) {
                view.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_leave_day));
            }
        });
    }

    private List<Insight> generateInsights(int totalDays, int presentDays, int leaveDays) {
        List<Insight> insights = new ArrayList<>();

        int absentDays = totalDays - (presentDays + leaveDays);
        float percentage = (presentDays * 100f) / totalDays;

        if (percentage >= 95) {
            insights.add(new Insight("Excellent attendance!", "You're maintaining a top record.", R.drawable.ic_star, R.color.success_color_light));
        } else if (percentage >= 80) {
            insights.add(new Insight("Good attendance", "Try to improve consistency.", R.drawable.ic_check_circle, R.color.warning_color_light));
        } else {
            insights.add(new Insight("Low attendance alert!", "Take steps to improve.", R.drawable.ic_warning, R.color.error_color_light));
        }

        if (leaveDays > 2) {
            insights.add(new Insight("Multiple leaves taken", "Make sure to balance your work schedule.", R.drawable.ic_calendar, R.color.warning_color_light));
        }

        if (absentDays == 0) {
            insights.add(new Insight("No absences this month", "Keep up the perfect streak!", R.drawable.ic_thumb_up, R.color.success_color_light));
        }

        return insights;
    }

    private void loadInsightsDataForCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        List<String> workingDates = getWorkingDaysForMonth(year, month);

        db.collection("attendance-data")
                .whereEqualTo("email", employeeEmail)
                .get()
                .addOnSuccessListener(attendanceDocs -> {

                    Set<String> presentDates = new HashSet<>();
                    for (QueryDocumentSnapshot doc : attendanceDocs) {
                        String date = doc.getString("date");
                        String type = doc.getString("type");
                        if (date != null && "login".equals(type)) {
                            presentDates.add(date);
                        }
                    }

                    db.collection("leave-requests")
                            .whereEqualTo("employeeEmail", employeeEmail)
                            .whereEqualTo("status", "approved")
                            .get()
                            .addOnSuccessListener(leaveDocs -> {

                                Set<String> leaveDates = new HashSet<>();
                                for (QueryDocumentSnapshot doc : leaveDocs) {
                                    String start = doc.getString("startDate");
                                    String end = doc.getString("endDate");
                                    if (start != null && end != null) {
                                        leaveDates.addAll(generateDateRange(start, end));
                                    }
                                }

                                int totalWorkingDays = workingDates.size();
                                int present = 0, leave = 0;

                                for (String date : workingDates) {
                                    if (presentDates.contains(date)) present++;
                                    else if (leaveDates.contains(date)) leave++;
                                }

                                // Now generate insights
                                List<Insight> insights = generateInsights(totalWorkingDays, present, leave);
                                displayInsights(insights);
                            });
                });
    }

    private void displayInsights(List<Insight> insights) {
        InsightAdapter adapter = new InsightAdapter(insights);
        rvInsights.setAdapter(adapter);
    }


}
