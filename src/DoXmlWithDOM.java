/*  
 * @info   
 * @author GuoJiang 
 * @time 2016年8月2日 下午4:39:56  
 * @version    
 */
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DoXmlWithDOM {

	private static List nameList = new ArrayList();
	private static List uiList = new ArrayList();
	private static List uiAdapterList = new ArrayList();
	private static List idList = new ArrayList();
	private static String fileName = "";

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		System.out.println("加载xml:");
		String url = sc.next();
		sc.close();

		File file = null;
		if (url.contains("'")) {
			file = new File(url.replace("'", ""));
		} else if (url.contains("file:")) {
			file = new File(url.replace("file:", ""));
		} else {
			file = new File(url);
		}

		// 获取文件名
		fileName = file.getName().split(".xml")[0];

		(new DoXmlWithDOM()).readXML(file);

		//如果是要生成adapter类，布局要以“item_”开头
		if (fileName.startsWith("item")) {
			createAdapter(fileName);
		//生成activity的信息	
		} else {
			createName();
			createUI();
			createId();
		}
	}

	/*
	 * 读取XML(文档对象-根元素节点-所有的Element类型节点-Text类型节点的内容) ;
	 * 获取文档对象:DocumentBuilderFactory → DocumentBuilder → Document
	 */
	public void readXML(File file) {
		// ❶Ⅰ获得DocumentBuilderFactory
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			// ❷Ⅱ获得DocumentBuilder
			DocumentBuilder builder = factory.newDocumentBuilder();
			// ❸Ⅲ--获得文档对象--
			Document doc = builder.parse(file);
			// ❹Ⅳ获得根元素
			Element element = doc.getDocumentElement();
			// ❺Ⅴ用方法遍历递归打印根元素下面所有的ElementNode(包括属性,TextNode非空的值),用空格分层次显示.
			listAllChildNodes(element, 0);// 参数0表示设定根节点层次为0,它的前面不打印空格.
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 递归遍历并打印所有的ElementNode(包括节点的属性和文本节点的有效内容),按一般的xml样式展示出来(空格来表示层次)
	 */
	public void listAllChildNodes(Node node, int level) {
		// 只处理ElementNode类型的节点,感觉这种类型的节点(还有有效的文本节点)才是真正有用的数据,其他注释节点,空白节点等都用不上.
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			boolean hasTextChild = false;// 变量表示该节点的第一个子节点是否就是一个有有效内容的文本节点)
			// Ⅰ❶【打印 - 空格】空格的长度 - level(n级ElementNode有n个长度的空格在前面)
			String levelSpace = "";
			for (int i = 0; i < level; i++) {
				levelSpace += "    ";
			}
			// Ⅱ❷【打印 - 开始标签】先打印ElementNode的开始标签(有属性的话也要打印)
			// System.out.print(levelSpace + "<" + node.getNodeName() +
			// (node.hasAttributes() ? " " : ">"));//
			// 有属性的话节点的开始标签后面的尖括号">"就留待属性打印完再打印

			// Ⅲ❸【打印 - 属性】遍历打印节点的所有属性
			if (node.hasAttributes()) {
				NamedNodeMap nnmap = node.getAttributes();
				for (int i = 0; i < nnmap.getLength(); i++) {
					// System.out.print(nnmap.item(i).getNodeName() + "=\""//
					// 字符串里含双引号要用到转义字符\
					// + nnmap.item(i).getNodeValue() + "\"" + (i ==
					// (nnmap.getLength() - 1) ? "" : " "));//
					// 不是最后一个属性的话属性之间要留空隙
					if (nnmap.item(i).getNodeName().equals("android:id")) {

						outResult(node.getNodeName(), nnmap.item(i).getNodeValue());
						// System.out.println(node.getNodeName());
						// System.out.println(nnmap.item(i).getNodeValue());

					}
				}
				// System.out.print(">");// 开始标签里的属性全部打印完加上尖括号">"
			}

			// Ⅳ❹【打印 - 子节点】该ElementNode包含子节点时候的处理
			if (node.hasChildNodes()) {
				level++;// 有下一级子节点,层次加1,新的层次表示的是这个子节点的层次(递归调用时传给了它)
				// 获得所有的子节点列表
				NodeList nodelist = node.getChildNodes();
				// 循环遍历取到所有的子节点
				for (int i = 0; i < nodelist.getLength(); i++) {
					// Ⅳ❹❶【有效文本子节点】子节点为TextNode类型,并且包含的文本内容有效
					if (nodelist.item(i).getNodeType() == Node.TEXT_NODE
							&& (!nodelist.item(i).getNodeValue().matches("\\s+"))) {// 用正则选取内容包含非空格的有效字符的文本节点
						hasTextChild = true;// 该ElementNode的一级子节点是存在有效字符的文本节点
						// System.out.print(nodelist.item(i).getNodeValue());//
						// 在开始标签后面添加文本内容
						// Ⅳ❹❷【ElementNode子节点】子节点是正常的ElementNode的处理
					} else if (nodelist.item(i).getNodeType() == Node.ELEMENT_NODE) {
						// System.out.println();
						// 递归调用方法 - 以遍历该节点下面所有的子节点
						listAllChildNodes(nodelist.item(i), level);// level表示该节点处于第几个层次(相应空格)
					}
				}
				level--;// 遍历完所有的子节点,层次变量随子节点的层数,依次递减,回归到该节点本身的层次
				// level++ 和 level--对于该节点的子节点影响的是子节点的初值
			}
			// Ⅴ❺【打印 - 结束标签】打印元素的结束标签.如果它的第一个一级子节点是有效文本的话,文本和结束标签添加到开始标签后面,
			// 层次什么的就作废用不上了,否则,才按层次打印结束标签.
			// System.out.print(((hasTextChild) ? "" : "\n" + levelSpace) + "</"
			// + node.getNodeName() + ">");
		}

	}

	/**
	 * 输出结果
	 */
	private void outResult(String name, String idValue) {
		// 处理类名
		if (name.contains(".")) {
			String[] n = name.split("\\.");
			name = n[n.length - 1];
		}

		// 处理ID
		String[] idArr = idValue.split("/");
		String id = idArr[1];

		String result = getUpperCase(id) + " = (" + name + ") findViewById(R.id." + id + ");\n";
		String click = getUpperCase(id) + ".setOnClickListener(this);\n";

		String result2 = "holder." + getUpperCase(id) + " = (" + name + ") convertView.findViewById(R.id." + id
				+ ");\n";

		nameList.add(name + " " + getUpperCase(id));
		uiList.add(result + click);
		uiAdapterList.add(result2);
		idList.add(id);
	}

	/**
	 * 取控件名生成_name
	 * 
	 * @param name
	 * @return
	 */
	private static String getUpperCase(String name) {

		if (name.contains("_")) {
			String[] ns = name.split("_");
			String all = "";
			for (int i = 0; i < ns.length; i++) {
				if (i == 0) {
					all = "_" + ns[i].substring(0, 1).toLowerCase() + ns[i].substring(1);
				} else {
					all += ns[i].substring(0, 1).toUpperCase() + ns[i].substring(1);
				}

			}
			return all;
		}

		return name;
	}

	/**
	 * 生成private值
	 * /home/ubt/workspace/WorkSpace_LuGangTong/LuGangTong/app/src/main
	 * /res/layouts/activity/layout/activity_add_bankcard.xml
	 */
	private static void createName() {
		System.out.println("\n\n=========== 生成private ===========\n");
		for (int i = 0; i < nameList.size(); i++) {
			String name = nameList.get(i).toString();
			System.out.println("private " + name + ";");
		}
		System.out.println();
	}

	private static String createNameForAdapter() {
		String viewHolderText = "";
		for (int i = 0; i < nameList.size(); i++) {
			String name = nameList.get(i).toString();
			viewHolderText += "\t\t" + name + "; \n";
		}
		return viewHolderText;
	}

	/**
	 * 生成UI值
	 */
	private static void createUI() {
		System.out.println("=========== 生成 R.id ===========\n");
		for (int i = 0; i < uiList.size(); i++) {
			System.out.println(uiList.get(i).toString());
		}
		System.out.println();
	}

	private static String createUIForAdapter() {
		String str = "";
		for (int i = 0; i < uiAdapterList.size(); i++) {
			str += "\t\t\t" + uiAdapterList.get(i).toString();
		}
		return str;
	}

	/**
	 * 生成OnClick值
	 */
	private static void createId() {
		System.out.println("=========== 生成 onclick ===========\n");
		for (int i = 0; i < idList.size(); i++) {
			String id = idList.get(i).toString();
			System.out.println("case R.id." + id + ":\n");
			System.out.println("break;\n");
		}
		System.out.println("=========== 生成 over ===========\n");
	}

	private static void createAdapter(String fileName) {
		String adapterStr = "========================= 生成 start ========================== \n"
				+ "import android.content.Context; \n" + "import android.view.LayoutInflater; \n"
				+ "import android.view.View; \n" + "import android.view.ViewGroup; \n"
				+ "import android.widget.BaseAdapter; \n" + "import android.widget.TextView; \n\n" +

				"import java.util.ArrayList; \n\n" +

				"/** \n" + " * @Info \n" + " * @Auth Bello \n" + " * @Time "
				+ getTime()
				+ " \n"
				+ " * @Ver \n"
				+ " */ \n"
				+ "public class "
				+ getAdapterClassName(fileName)
				+ " extends BaseAdapter { \n"
				+ "\tprivate Context mContext; \n"
				+ "\tprivate ArrayList<T> mListItems; \n\n"
				+

				"\tpublic "
				+ getAdapterClassName(fileName)
				+ "(Context mContext, ArrayList<T> mListItems) { \n"
				+ "\t\tthis.mContext = mContext; \n"
				+ "\t\tthis.mListItems = mListItems; \n"
				+ "\t} \n\n"
				+

				"\t@Override \n"
				+ "\tpublic int getCount() { \n"
				+ "\t\treturn mListItems.size(); \n"
				+ "\t} \n\n"
				+

				"\t@Override \n"
				+ "\tpublic Object getItem(int position) { \n"
				+ "\t\treturn mListItems.get(position); \n"
				+ "\t} \n\n"
				+

				"\t@Override \n"
				+ "\tpublic long getItemId(int position) { \n"
				+ "\t\treturn position; \n"
				+ "\t} \n\n"
				+

				"\t@Override \n"
				+ "\tpublic View getView(int position, View convertView, ViewGroup parent) { \n"
				+ "\t\tViewHolder holder; \n"
				+ "\t\tif (convertView == null){ \n"
				+ "\t\t\tconvertView = LayoutInflater.from(mContext).inflate(R.layout."
				+ fileName
				+ ", null); \n"
				+ "\t\t\tholder = new ViewHolder(); \n"
				+

				createUIForAdapter()
				+

				"\t\t\tconvertView.setTag(holder); \n"
				+ "\t\t} else { \n"
				+ "\t\t\tholder = (ViewHolder) convertView.getTag(); \n"
				+ "\t\t} \n\n"
				+

				"\t\t// TODO \n\n"
				+

				"\t\treturn convertView; \n"
				+ "\t} \n\n"
				+

				"\tclass ViewHolder { \n"
				+ createNameForAdapter()
				+ "\t} \n"
				+ "} \n\n"
				+ "=============== 生成 over ================";

		System.out.println(adapterStr);

	}

	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	public static String getTime() {
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}

	public static String getAdapterClassName(String name) {
		name = getUpperCase(name).replace("_", "");
		if (name.contains("item")) {
			name = name.replace("item", "");
		}
		return name + "Adapter";
	}
}
