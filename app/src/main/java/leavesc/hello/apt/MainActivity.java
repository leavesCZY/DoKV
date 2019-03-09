package leavesc.hello.apt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import leavesc.hello.apt.model.Book;
import leavesc.hello.apt.model.User;
import leavesc.hello.apt.model.UserPreferences;
import leavesc.hello.apt_annotation.BindView;

/**
 * 作者：leavesC
 * 时间：2019/1/4 23:15
 * 描述：
 * GitHub：https://github.com/leavesC
 * Blog：https://www.jianshu.com/u/9df45b87cfdf
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.et_userName)
    EditText et_userName;

    @BindView(R.id.et_userAge)
    EditText et_userAge;

    @BindView(R.id.et_bookName)
    EditText et_bookName;

    @BindView(R.id.btn_serializeAll)
    Button btn_serializeAll;

    @BindView(R.id.et_singleUserName)
    EditText et_singleUserName;

    @BindView(R.id.btn_serializeSingle)
    Button btn_serializeSingle;

    @BindView(R.id.btn_remove)
    Button btn_remove;

    @BindView(R.id.btn_print)
    Button btn_print;

    @BindView(R.id.tv_hint)
    TextView tv_hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);
        MainActivityViewBinding.bind(this);
        btn_serializeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = et_userName.getText().toString();
                String ageStr = et_userAge.getText().toString();
                int age = 0;
                if (!TextUtils.isEmpty(ageStr)) {
                    age = Integer.parseInt(ageStr);
                }
                String bookName = et_bookName.getText().toString();
                User user = new User();
                user.setAge(age);
                user.setName(userName);
                user.setSex("xxxx");
                Book book = new Book();
                book.setName(bookName);
                user.setBook(book);
                UserPreferences.get().setUser(user);
            }
        });
        btn_serializeSingle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = et_singleUserName.getText().toString();
                UserPreferences.get().setName(userName);
            }
        });
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPreferences.get().remove();
            }
        });
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = UserPreferences.get().getUser();
                if (user == null) {
                    tv_hint.setText("null");
                } else {
                    tv_hint.setText(user.toString());
                }
            }
        });
    }
}
