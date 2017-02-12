/************************************************************************
 * *
 * * Java class MainActivity  craeted by Emmanuel Yeboah.
 * Under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *
 * *
 * ***********************************************************************/

package com.example.emmanuel.client_sample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.widget.LinearLayout;

import android.widget.Toast;
import android.widget.TextView;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import android.graphics.Color;

import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends Activity  {

    LinearLayout ConnectPanel, ColorPalettePanel;
    TextView textResponse;
    Button buttonConnect, buttonClear,buttonPause;
    Button  buttonsendcolor;

    String messageLog = "";

    ClientThreadTask client_threadTask = null;
    private ColorPicker colorPalette;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //buttonConnect = (Button) findViewById(R.id.btn_connect);
        buttonClear = (Button) findViewById(R.id.btn_clear);
        textResponse = (TextView)findViewById(R.id.txt_response);

        buttonPause =(Button)findViewById(R.id.btn_pause);

        buttonsendcolor =  (Button) findViewById(R.id.btn_SendColor);
        //buttonActivateColorpanel = (Button) findViewById(R.id.btn_colorpalette);

        //buttonConnect.setOnClickListener(btnConnectOnClickListener);
        buttonsendcolor.setOnClickListener(btnSendColorOnClickListener);
       // buttonActivateColorpanel.setOnClickListener(btnActivateColorPanelOnClickListener);
        buttonClear.setOnClickListener(btnSendClearOnClickListener);
        buttonPause.setOnClickListener(btnSendPauseOnClickListener);

        colorPalette =(ColorPicker) findViewById(R.id.colorPicker);

        // for the panels initialization
        ColorPalettePanel =(LinearLayout) findViewById(R.id.colorpalattepanel);
        ConnectPanel = (LinearLayout) findViewById(R.id.connectpanel);
    }


    public void ShowConnectionToESP8288AP_Dialog(View view)
    {
        if(!isESP8288AP_connected() )
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            //alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setTitle("Aw, snap!");
            alertDialogBuilder.setMessage("It seems you are not connected to ESP8288AP \nTo continue, select 'settings' to change your network \n ");
            alertDialogBuilder.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    //Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                    MainActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        else if (isESP8288AP_connected() )  {
            client_threadTask = new ClientThreadTask("192.168.4.1",1001);
            client_threadTask.start();
            ConnectPanel.setVisibility(View.GONE);
            ColorPalettePanel.setVisibility(View.VISIBLE);
        }

    }


    public boolean isESP8288AP_connected (){
        boolean connnectedEsp8288AP = false;

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if(wifiManager == null)
        {
            return connnectedEsp8288AP;
        }
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == android.net.NetworkInfo.DetailedState.CONNECTED)
        {

           if(wifiInfo.getSSID().equals("ESP8288AP"))
            {
                connnectedEsp8288AP = true;

                return connnectedEsp8288AP;

            }
        }
        SupplicantState state = wifiManager.getConnectionInfo().getSupplicantState();
        if (state== null){
            return connnectedEsp8288AP;
        }
        return connnectedEsp8288AP;
}



    OnClickListener btnSendColorOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int color = colorPalette.getColor();
            int R = Color.red(color) ;
            int B  = Color.blue(color) ;
            int G = Color.green(color);
            char RInChar = (char)R;
            char BInChar = (char)B;
            char GInChar = (char)G;
            //String RGBtoHex= String.format("#%02X%02X%02X",Color.red(color),Color.blue(color),Color.green(color));
            //String RGBtoString = "R: "+ R +" B : "+ B +" G: "+ G;

            //Toast.makeText(MainActivity.this,RGBtoString,Toast.LENGTH_LONG).show();

            //String SubstringRGBtoHex = RGBtoHex.replace("#","");

            client_threadTask.sendMessageToServer(" "+RInChar+BInChar+GInChar+"\n");

        }
    };

    OnClickListener btnSendClearOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String ClearContentASCII = "CLEAR"+"\n";
            client_threadTask.sendMessageToServer(ClearContentASCII);
            //Toast.makeText(MainActivity.this,"Content Cleared",Toast.LENGTH_LONG).show();
        }
    };

    OnClickListener btnSendPauseOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String PauseContentASCII = "STOP"+"\n";
            client_threadTask.sendMessageToServer(PauseContentASCII);
            //Toast.makeText(MainActivity.this,"Content Paused",Toast.LENGTH_LONG).show();
        }
    };



    public void AboutUs_Dialog(View view)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        //alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setTitle("About Us!");
        alertDialogBuilder.setMessage("This is an experimental app for a research and design project for " +
                "controlling content generated on a Altera FPGA and displayed on LED matrix .\n" +
                "\nProject Supervised by: Tekin Yilmaz \n" +
                "\nProject members: Emmanuel Yeboah, Ragy Guirguis, Fedor Zorin \n " +
                "\nLicensed under the Apache License, Version 2.0 ");
        alertDialogBuilder.setPositiveButton(R.string.about_us, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

   //OnClickListener btnConnectOnClickListener = new OnClickListener() {
     //   @Override
       // public void onClick(View v) {

        //}
    //};


    public class ClientThreadTask extends Thread{

        String DestinationAddr;
        int DestinationPort;
        //String ResponseFromServer = "";

        String MsgToServer = "";
        boolean OptOut = false;


        ClientThreadTask(String addr, int port){
            DestinationAddr = addr;
            DestinationPort = port;
           // MsgToServer = Msg;

        }

        @Override
        public void run() {
            Socket socket = null;

            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try{
                socket = new Socket(DestinationAddr,DestinationPort);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());



                /**ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buf = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

                while((bytesRead = inputStream.read(buf)) != -1){
                    byteArrayOutputStream.write(buf,0, bytesRead);
                    ResponseFromServer +=byteArrayOutputStream.toString("UTF-8");
                }**/




                while(!OptOut){
                    if(dataInputStream.available() > 0){

                        messageLog += dataInputStream.readChar();
                       MainActivity.this.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               textResponse.setText(messageLog);
                           }
                       });

                    }
                    if(!MsgToServer.equals("")){
                        dataOutputStream.writeChars(MsgToServer);
                        dataOutputStream.flush();
                        MsgToServer = "";
                    }


                }

            } catch (UnknownHostException e){
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,eString,Toast.LENGTH_LONG).show();
                    }
                });

             } catch (IOException e){
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,eString,Toast.LENGTH_LONG).show();
                    }
                });

            } finally{
                if(socket != null) {
                    try {
                        socket.close();
                        Toast.makeText(MainActivity.this, "Socket closed", Toast.LENGTH_LONG).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        String exception_closing = "IOEXception closing socket :" + e.toString();
                        Toast.makeText(MainActivity.this, exception_closing, Toast.LENGTH_LONG).show();
                    }
                }

                    if(dataInputStream != null ){
                        try{
                            dataInputStream.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }

                    if(dataOutputStream != null){
                        try{
                            dataOutputStream.close();
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ConnectPanel.setVisibility(View.VISIBLE);
                        ColorPalettePanel.setVisibility(View.GONE);

                    }
                });
                }


        }
        private void sendMessageToServer(String msg){
            MsgToServer = msg;
        }
        private void disconnect(){
            OptOut = true;
        }


        /**@Override
        protected void onPostExecute(Void result){
            textResponse.setText(ResponseFromServer);
            super.onPostExecute(result);
        }**/

    }
}

