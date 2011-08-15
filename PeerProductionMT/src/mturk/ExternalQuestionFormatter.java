package mturk;

import org.apache.commons.lang.StringEscapeUtils;

public class ExternalQuestionFormatter {
    public static String createQuestion(String url, int height){
        StringBuffer buf = new StringBuffer(); 
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ExternalQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2006-07-14/ExternalQuestion.xsd\">\n");
        buf.append("<ExternalURL>").append(StringEscapeUtils.escapeXml(url)).append("</ExternalURL>\n");
        buf.append("<FrameHeight>").append(height).append("</FrameHeight>\n");
        buf.append("</ExternalQuestion>\n");
        return buf.toString();
    }
}