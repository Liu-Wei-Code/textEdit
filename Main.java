import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        MyFrame myFrame = new MyFrame();
    }

    static class MyFrame extends JFrame {
        private JTextArea textArea = null;
        private JMenu[] menuList = new JMenu[2];
        private String filePath = null;
        private String fileEncode = null;
        private String fileName = null;
        private boolean alreadySave = false;

        MyFrame() {
            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    alreadySave = false;
                    setTitleName();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    alreadySave = false;
                    setTitleName();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    alreadySave = false;
                    setTitleName();
                }
            });
            JScrollPane jScrollPane = new JScrollPane(textArea);
            jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            this.add(jScrollPane);

            this.addMenu();

            this.setVisible(true);
            this.setSize(600, 400);
            this.setLocationRelativeTo(null);//让窗口居中
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setTitle("土豆的文本编辑器");

        }

        private void addMenu() {
            JMenuBar menuBar = new JMenuBar();
            menuList[0] = new JMenu("文件");
            menuBar.add(menuList[0]);
            JMenuItem jMenuItemNew = new JMenuItem("新建");
            menuList[0].add(jMenuItemNew);
            JMenuItem jMenuItemOpen = new JMenuItem("打开");
            jMenuItemOpen.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    readFile();
                }
            });
            menuList[0].add(jMenuItemOpen);
            JMenuItem jMenuItemSave = new JMenuItem("保存");
            jMenuItemSave.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveFile();
                }
            });
            menuList[0].add(jMenuItemSave);
            JMenuItem jMenuItemSaveAs = new JMenuItem("另存为");
            jMenuItemSaveAs.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveAsFile();
                }
            });
            menuList[0].add(jMenuItemSaveAs);
            menuList[0].addSeparator();
            JMenuItem jMenuItemExit = new JMenuItem("退出");
            jMenuItemExit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            menuList[0].add(jMenuItemExit);


            menuList[1] = new JMenu("关于");
            menuBar.add(menuList[1]);
            JMenuItem jMenuItemAbout = new JMenuItem("查看关于");
            menuList[1].add(jMenuItemAbout);
            jMenuItemAbout.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, "此程序由土豆制作用来练习文件操作和GUI编程，仅供学习！", "关于", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            this.setJMenuBar(menuBar);
        }

        /**
         * 检测要打开文件的编码格式
         *
         * @param file 要检测的文件
         * @return 编码格式
         */
        private String charSet(File file) {
            String encode = "gbk";
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                CharsetDetector charsetDetector = new CharsetDetector();
                charsetDetector.setText(bufferedInputStream);
                CharsetMatch charsetMatch = charsetDetector.detect();
                CharsetMatch[] matches = charsetDetector.detectAll();
                //过滤编码，UTF-8，UTF-16LE，UTF-16BE，GB2312，GB18030，GBK，ASCII，默认为GBK编码
                for (CharsetMatch m : matches) {
                    String str = m.getName();
                    if (str.equalsIgnoreCase("UTF-8") || str.equalsIgnoreCase("GBK") || str.equalsIgnoreCase("GB18030")
                            || str.equalsIgnoreCase("GB2312") || str.equalsIgnoreCase("ASCII")
                            || str.equalsIgnoreCase("UTF-16LE") || str.equalsIgnoreCase("UTF-16BE")){
                        encode = str;
                        break;
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(encode);
            return encode;

        }

        /**
         * 读取文件
         */
        private void readFile() {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.showOpenDialog(null);
            File file = jFileChooser.getSelectedFile();
            String str = null;
            BufferedReader reader = null;
            if (file != null) {
                try {
                    String code = charSet(file);
                    filePath = file.getAbsolutePath();
                    fileEncode = code;
                    fileName = file.getName();
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), code));
                    textArea.setText("");
                    while ((str = reader.readLine()) != null) {
                        textArea.append(str + "\r\n");

                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                textArea.setCaretPosition(0);//使光标移动到第一行
                alreadySave = true;
                setTitleName();
            }
        }

        /**
         * 保存文件
         */
        private void saveFile() {
            String str = textArea.getText().replaceAll("\n", "\r\n");
            if (filePath != null) {
                try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath)), fileEncode))) {
                    bufferedWriter.write(str);
                    alreadySave = true;
                    setTitleName();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                saveAsFile();
            }
        }

        /**
         * 另存为
         */
        private void saveAsFile() {
            String str = textArea.getText().replaceAll("\n", "\r\n");
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(null);
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                filePath = file.getAbsolutePath();
                fileName = file.getName();
                fileEncode = "GBK";
                try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), fileEncode))) {
                    bufferedWriter.write(str);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                alreadySave = true;
                setTitleName();
            }

        }

        /**
         * 设置标题
         */
        private void setTitleName() {
            if (fileName == null) {
                this.setTitle("土豆的文本编辑器----*未命名.txt");
            } else if (alreadySave) {
                this.setTitle("土豆的文本编辑器----" + fileName);
            } else {
                this.setTitle("土豆的文本编辑器----*" + fileName);
            }
        }
    }
}
