# CommunicationLib
1. Add the Maven reference into your gradle file.
                                   
                                    allprojects 
                                    {
                                        repositories 
                                        {
                                            maven 
                                            { url 'https://jitpack.io' }
                                        }
                                    }
                                
                            
2. Add the gradle reference to last version api.
                                   
                                    dependencies 
                                    {
                                          implementation 'com.github.siggytech:notificatorlib:v3.1.3'
                                    }
                                
                            
3. Improve the next code for Push To Talk service:
                                public class MainActivity extends Activity {

    PTTButton pttButton;
    LinearLayout linearLayout;
    String API_KEY = "YOURAPIKEY";
    String name = "YOURCLIENTNAME";
    Integer groupId = 1; //set a custom id group

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Conf.SERVER_IP = "YOUR PRIVATE IP ADDRESS"; 
        addPTTButton();
    }
    private void addPTTButton(){
        linearLayout = findViewById(R.id.linear1);
        pttButton = new PTTButton(this, this, groupId, API_KEY, name);

        pttButton.setWidth(200);
        pttButton.setHeight(200);
        pttButton.setText("Let´s talk!");

        linearLayout.addView(pttButton);


    }
}
                                    
                                
                            
Improve the next code for Private Chat service:
                                public class MainActivity extends Activity {

    ChatControl ch;
    LinearLayout linearLayout;
    String API_KEY = "YOURAPIKEY";
    String name = "YOURCLIENTNAME";
    String username = "Jhon Doe";
    Integer groupId = 1; //set a custom id group

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Conf.SERVER_IP = "YOUR PRIVATE IP ADDRESS"; 
        addPTTButton();
    }
    public void addChatListView()
    {
        Conf.DATE_FORMAT = 1; //1: mm-dd-yyyy hh:mm:ss - 2: dd-mm-yyyy hh24:mm:ss
        Conf.LOCAL_USER = "Me"; //user name to show in my device. Default: Me
        ch = new ChatControl(this, groupId, API_KEY, name, username);//user name to show to others
        linearLayout.addView(ch);
    }
}
                                    
                                
                            
