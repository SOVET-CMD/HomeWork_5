package com.example.homework_5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;

import kotlin.Metadata;

public class MainActivity extends AppCompatActivity  implements Runnable{

    // создание полей
    private MediaPlayer mediaPlayer = new MediaPlayer(); // создание поля медиа-плеера
    private SeekBar seekBar; // создание поля SeekBar
    private boolean wasPlaying = false; // поле проигрывания аудио-файла
    private FloatingActionButton fabPlayPause; // поле кнопки проигрывания и постановки на паузу аудиофайла
    private TextView seekBarHint; // поле информации у SeekBar
    int TrackPosition;// переменная позиции трека
    private FloatingActionButton retri;//повтор трека
    private FloatingActionButton forward; // поле кнопки перемотки вперед
    private FloatingActionButton back; // поле кнопки перемотки назад
    private FloatingActionButton NextTrack;//поле кнопки переключения трека вперед
    private TextView NameAudio;//поле название трека
    private String Name;//переменая для сохранения имени трека
    boolean isReturn = true;
    private ImageView ImgAudio;// картинка песни
    byte[] Img;
    int Track = 0;
    // Создания массива из названий файлов
    String[] NameFile = new String[] {"Korol_i_SHut_-_Lesnik_(musmore.com).mp3","Korol_i_SHut_-_Eli_myaso_muzhiki_(musmore.com).mp3","Korol_i_SHut_-_Kukla_kolduna_(musmore.com).mp3"};
    String NameSound ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // присваивание полям id ресурсов
        fabPlayPause = findViewById(R.id.fabPlayPause);
        seekBarHint = findViewById(R.id.seekBarHint);
        seekBar = findViewById(R.id.seekBar);
        forward = findViewById(R.id.forward);
        back = findViewById(R.id.Back);
        NextTrack = findViewById(R.id.NextTrack);
        NameAudio = findViewById(R.id.NameAudio);
        retri = findViewById(R.id.Return);
        ImgAudio = findViewById(R.id.ImgAudio);
        // создание слушателей нажатия кнопок
        fabPlayPause.setOnClickListener(Listener);
        if (mediaPlayer!= null){
            NextTrack.setOnClickListener(Listener);
            forward.setOnClickListener(Listener);
            back.setOnClickListener(Listener);
            retri.setOnClickListener(Listener);}
        // создание слушателя изменения SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // метод при перетаскивании ползунка по шкале,
            // где progress позволяет получить нове значение ползунка (позже progress назрачается длина трека в миллисекундах)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                TrackPosition = progress; //присвоение переменой значения Progress (положения бегунка)
                seekBarHint.setVisibility(View.VISIBLE); // установление видимости seekBarHint
                //seekBarHint.setVisibility(View.INVISIBLE); // установление не видимости seekBarHint

                // Math.ceil() - округление до целого в большую сторону
                int timeTrack = (int) Math.ceil(progress/1000f); // перевод времени из миллисекунд в секунды

                // вывод на экран времени отсчёта трека
                if (timeTrack < 10) {
                    seekBarHint.setText("00:0" + timeTrack);
                } else if (timeTrack < 60){
                    seekBarHint.setText("00:" + timeTrack);
                } else if (timeTrack >= 60) {
                    seekBarHint.setText("01:" + (timeTrack - 60));
                }

                // передвижение времени отсчёта трека
                double percentTrack = progress / (double) seekBar.getMax(); // получение процента проигранного трека (проигранное время делится на длину трека)
                // seekBar.getX() - начало seekBar по оси Х
                // seekBar.getWidth() - ширина контейнера seekBar
                // 0.92 - поправочный коэффициент (так как seekBar занимает не всю ширину своего контейнера)
                seekBarHint.setX(seekBar.getX() + Math.round(seekBar.getWidth()*percentTrack*0.92));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer не воспроизводится
                    clearMediaPlayer(); // остановка и очиска MediaPlayer
                    // назначение кнопке картинки play
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                }
            }
            // метод при начале перетаскивания ползунка по шкале
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.INVISIBLE); // установление видимости seekBarHint
            }
            // метод при завершении перетаскивания ползунка по шкале
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                    mediaPlayer.seekTo(seekBar.getProgress()); // обновление позиции трека при изменении seekBar
                }
            }
        });
    }

    // метод запуска аудио-файла
    public void playSong() {
        try { // обработка исключения на случай отстутствия файла
            if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                clearMediaPlayer(); // остановка и очиска MediaPlayer
                wasPlaying = true; // инициализация значения запуска аудио-файла
                // назначение кнопке картинки play
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
            }

            if (!wasPlaying) {
                if (mediaPlayer == null) { // если mediaPlayer пустой
                    mediaPlayer = new MediaPlayer(); // то выделяется для него память
                }
                // назначение кнопке картинки pause
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                // альтернативный способ считывания файла с помощью файлового дескриптора
                AssetFileDescriptor descriptor = getAssets().openFd(NameSound = NameFile[Track]);

                // запись файла в mediaPlayer, задаются параметры (путь файла, смещение относительно начала файла, длина аудио в файле)
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());

                mediaPlayer.prepare(); // ассинхронная подготовка плейера к проигрыванию
                //mediaPlayer.setVolume(0.7f, 0.7f); // задание уровня громкости левого и правого динамиков
                seekBar.setMax(mediaPlayer.getDuration()); // ограниечение seekBar длинной трека

                try {

                    //извлечение названия и автора и картинки
                    MediaMetadataRetriever NameData = new MediaMetadataRetriever();
                    NameData.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    Img = NameData.getEmbeddedPicture();
                    Bitmap TrackImg = BitmapFactory.decodeByteArray(Img,0,Img.length);
                    Name = NameData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);//полчения имени артиста
                    Name = Name + " - " + NameData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);// получение названия трека и обьединение имени артиста и песни
                    ImgAudio.setImageBitmap(TrackImg);
                    NameData.release();//освоюождение ресурсов
                }
                catch (Exception e){}
                NameAudio.setText(Name);//вывод на TextView
                mediaPlayer.start(); // старт mediaPlayer
                mediaPlayer.seekTo(TrackPosition);// изменение позиции трека
                new Thread(this).start(); // запуск дополнительного потока
            }

            wasPlaying = false; // возврат отсутствия проигрывания mediaPlayer

        } catch (Exception e) { // обработка исключения на случай отстутствия файла
            e.printStackTrace(); // вывод в консоль сообщения отсутствия файла
        }

    }

    // при уничтожении активити вызов метода остановки и очиски MediaPlayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    private final View.OnClickListener Listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fabPlayPause:
                    playSong();
                    break;

                case R.id.forward:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                    break;
                case R.id.Back:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                    break;
                case R.id.Return:
                    if (!isReturn ) {
                        mediaPlayer.setLooping(false);
                        retri.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_menu_sort_alphabetically));
                        isReturn = true;
                    } else {
                        mediaPlayer.setLooping(true);
                        retri.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_popup_sync));
                        isReturn = false;
                    }
                    break;
                case R.id.NextTrack:
                    if(Track<(NameFile.length-1)){
                        Track= Track+1;}
                    else if(Track>=(NameFile.length-1)){
                        Track=0;
                    }
                    clearMediaPlayernext();
                    break;
            }
        }
    };
    private void clearMediaPlayernext() {
        mediaPlayer.release(); // освобождение ресурсов
        mediaPlayer = null; // обнуление mediaPlayer
        playSong();//Запуск Метода
        MainActivity.this.seekBar.setProgress(0); // установление seekBar значения 0
    }

    // метод остановки и очиски MediaPlayer
    private void clearMediaPlayer() {
        mediaPlayer.stop(); // остановка медиа
        mediaPlayer.release(); // освобождение ресурсов
        mediaPlayer = null; // обнуление mediaPlayer
    }

    // метод дополнительного потока для обновления SeekBar
    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition(); // считывание текущей позиции трека
        int total = mediaPlayer.getDuration(); // считывание длины трека

        // бесконечный цикл при условии не нулевого mediaPlayer, проигрывания трека и текущей позиции трека меньше длины трека
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {

                Thread.sleep(1000); // засыпание вспомогательного потока на 1 секунду
                currentPosition = mediaPlayer.getCurrentPosition(); // обновление текущей позиции трека

            } catch (InterruptedException e) { // вызывается в случае блокировки данного потока
                e.printStackTrace();
                return; // выброс из цикла
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition); // обновление seekBar текущей позицией трека

        }
    }
}