package Scoring.mle.features;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import Scoring.FeatureManager;
import Scoring.mle.MLEFeature;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.nonterminals.NonterminalTransformerFactory;
import Utils.MyUtils;

public class MLEFeatureFactory {

	public static MLEFeature get(String name, FeatureManager fman) throws RuntimeException {

		try {
			String[] toks = MyUtils.tokenize(name, MLEFeature.FEATURE_NAME_DELIM);
			if (toks.length != 3) {
				throw new RuntimeException(
						"Expected format of feature names is FeatureClassName_NonterminalTransformerClassName_SmoothingCount, not: " + name);
			}
			String statName = toks[0];
			String transformerName = toks[1];
			float smoothCount = Float.parseFloat(toks[2]);

			String statClassName = MLEFeatureFactory.class.getPackage().getName() + "." + statName;
			Class<MLEFeature> statClass = (Class<MLEFeature>) Class.forName(statClassName);

			NonterminalTransformer transformer = NonterminalTransformerFactory.get(transformerName);
			assert transformer != null;

			Constructor<MLEFeature> constructor =
					statClass.getConstructor(FeatureManager.class, NonterminalTransformer.class,
							float.class);
			MLEFeature stat = constructor.newInstance(fman, transformer, smoothCount);
			return stat;

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Class for feature name not found: " + name, e);
		}
	}
}
