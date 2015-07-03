package produceXML;


//produce requests using dom4j
//http://lolikitty.pixnet.net/blog/post/128316581-�Ը�-java-xml-�Ы�-Ū��-(�ϥΡGdom4j)


import java.io.*;
import java.util.*;

import org.dom4j.*;
import org.dom4j.io.*;

//import java.nio.charset.Charset;


public class requestXMLvector {
	
	Document doc = DocumentHelper.createDocument();
	Element root = doc.addElement("Request", "urn:oasis:names:tc:xacml:2.0:context:schema:os"); // �إ߮ڤ���<Request>
	//solution: http://stackoverflow.com/questions/7809216/dom4j-xmlns-attribute
	
	Vector<Element> childrenVector = new Vector<Element>();
	Vector<Element> attributeVector = new Vector<Element>();
	Vector<Element> valueVector = new Vector<Element>();
	//0: Subject
	//1: Resource
	//2: Action
	//3: Environment
	
//	Element Subject = root.addElement("Subject"); // �b<Request>���U�إ� <Subject>
//	Element SubjectAttribute = Subject.addElement("Attribute"); // �b<Subject>���U�إ� <Attribute>
//	Element SubjectValue = SubjectAttribute.addElement("AttributeValue"); // �bSubject��<Attribute>���U�إ� <AttributeValue>
	
//	Element Resource = root.addElement("Resource"); // �b<Request>���U�إ� <Resource>
//	Element ResourceAttribute = Resource.addElement("Attribute"); // �b<Resource>���U�إ� <Attribute>
//	Element ResourceValue = ResourceAttribute.addElement("AttributeValue"); // �bResource��<Attribute>���U�إ� <AttributeValue>
	
//	Element Action = root.addElement("Action"); // �b<Request>���U�إ� <Action>
//	Element ActionAttribute = Action.addElement("Attribute"); // �b<Action>���U�إ� <Attribute>
//	Element ActionValue = ActionAttribute.addElement("AttributeValue"); // �bAction��<Attribute>���U�إ� <AttributeValue>
	
//	Element Environment = root.addElement("Environment"); // �b<Request>���U�إ� <Environment>
//	Element EnvironmentAttribute = Environment.addElement("Attribute"); // �b<Environment>���U�إ� <Attribute>
//	Element EnvironmentValue = EnvironmentAttribute.addElement("AttributeValue"); // �bEnvironment��<Attribute>���U�إ� <AttributeValue>
	

	public requestXMLvector(){
		
		root.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.addAttribute("xsi:schemaLocation", "urn:oasis:names:tc:xacml:2.0:context:schema:os xacml-2.0-context.xsd");
	}
	
	public void addSubject(String str){
		
		Element Subject = root.addElement("Subject");
		Element SubjectAttribute = Subject.addElement("Attribute");
		Element SubjectValue = SubjectAttribute.addElement("AttributeValue");
		
		SubjectAttribute.addAttribute("AttributeId", "urn:oasis:names:tc:xacml:1.0:subject:subject-id");
		SubjectAttribute.addAttribute("DataType", "http://www.w3.org/2001/XMLSchema#string");
		
		SubjectValue.addText( str );
		
		childrenVector.add(Subject);
		attributeVector.add(SubjectAttribute);
		valueVector.add(SubjectValue);
	}
	
	public void addResource(String str){
		
		Element Resource = root.addElement("Resource");
		Element ResourceAttribute = Resource.addElement("Attribute");
		Element ResourceValue = ResourceAttribute.addElement("AttributeValue");
		
		ResourceAttribute.addAttribute("AttributeId", "urn:oasis:names:tc:xacml:1.0:resource:resource-id");
		ResourceAttribute.addAttribute("DataType", "http://www.w3.org/2001/XMLSchema#string");
		
		ResourceValue.addText( str );
		
		childrenVector.add(Resource);
		attributeVector.add(ResourceAttribute);
		valueVector.add(ResourceValue);
	}
	
	public void addAction(String str){
		
		Element Action = root.addElement("Action");
		Element ActionAttribute = Action.addElement("Attribute");
		Element ActionValue = ActionAttribute.addElement("AttributeValue");
		
		ActionAttribute.addAttribute("AttributeId", "urn:oasis:names:tc:xacml:1.0:action:action-id");
		ActionAttribute.addAttribute("DataType", "http://www.w3.org/2001/XMLSchema#string");
		
		ActionValue.addText( str );
		
		childrenVector.add(Action);
		attributeVector.add(ActionAttribute);
		valueVector.add(ActionValue);
	}
	
	public void addEnvironment(String str){
		
		Element Environment = root.addElement("Environment");
		Element EnvironmentAttribute = Environment.addElement("Attribute");
		Element EnvironmentValue = EnvironmentAttribute.addElement("AttributeValue");
		
		EnvironmentAttribute.addAttribute("AttributeId", "urn:oasis:names:tc:xacml:1.0:environment:environment-id");
		EnvironmentAttribute.addAttribute("DataType", "http://www.w3.org/2001/XMLSchema#string");
		
		EnvironmentValue.addText( str );
		
		childrenVector.add(Environment);
		attributeVector.add(EnvironmentAttribute);
		valueVector.add(EnvironmentValue);
	}
	public void addBlankEnvironment(){
		Element Environment = root.addElement("Environment");
		childrenVector.add(Environment);
	}
	
	public void saveXML( String dir ) {
		
		try {
			FileWriter fw = null;
			fw = new FileWriter( dir ); // ���w�x�s��m
			
			OutputFormat of = OutputFormat.createPrettyPrint(); 
			XMLWriter xw = new XMLWriter(fw, of);
			
			xw.write(doc);
			xw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void buildXML( String dir ){ //dir��request���|
		
		System.out.println( );
		System.out.println("New request:");
		
		String str1, str2, str3;
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Set the Subject: ");
		str1 = scanner.nextLine();
		System.out.print("Set the Action: ");
		str3 = scanner.nextLine();
		System.out.print("Set the Resource: ");
		str2 = scanner.nextLine();
		
		addSubject(str1);
		addResource(str2);
		addAction(str3);
		
//		System.out.print("Do you want to set Environment? (Y/N)");
//		str = scanner.nextLine();
//		if(str.equalsIgnoreCase("Y")||str.equalsIgnoreCase("yes")){
//			System.out.print("Set the Environment: ");
//			str = scanner.nextLine();
//			addEnvironment(str);
//		}
//		else{
			addBlankEnvironment();
//		}
		
		
		scanner.close();
		scanner = null;
		saveXML( dir );
		System.out.println("Request built.");
	}

} //requestXMLvector end