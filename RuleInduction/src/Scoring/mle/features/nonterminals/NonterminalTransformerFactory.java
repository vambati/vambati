package Scoring.mle.features.nonterminals;


public class NonterminalTransformerFactory {

	public static NonterminalTransformer get(String transformerName) {

		String transformerClassName =
				NonterminalTransformer.class.getPackage().getName() + "." + transformerName;
		Class<NonterminalTransformer> transformerClass;
		try {
			transformerClass = (Class<NonterminalTransformer>) Class.forName(transformerClassName);
			NonterminalTransformer transformer = transformerClass.newInstance();
			return transformer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
