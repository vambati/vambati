package Scoring.mle.features.suffstat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import Scoring.FeatureManager;
import Scoring.mle.MLEFeature;
import Scoring.mle.MLESuffStat;
import Scoring.mle.features.nonterminals.NonterminalTransformer;
import Scoring.mle.features.nonterminals.NonterminalTransformerFactory;
import Utils.MyUtils;

public class SuffStatFactory {

	public static MLESuffStat get(String name, FeatureManager fman) throws RuntimeException {

		String[] toks = MyUtils.tokenize(name, MLEFeature.FEATURE_NAME_DELIM);
		String statName = toks[0];
		String transformerName = toks[1];
		if (toks.length != 2) {
			throw new RuntimeException(
					"Expected format of sufficient stat names is SuffStatClassName_NonterminalTransformerClassName, not: " + name	);
		}
		
		String statClassName = SuffStatFactory.class.getPackage().getName() + "." + statName;

		try {
			NonterminalTransformer transformer = NonterminalTransformerFactory.get(transformerName);

			Class<MLESuffStat> statClass = (Class<MLESuffStat>) Class.forName(statClassName);
			Constructor<MLESuffStat> constructor =
					statClass.getConstructor(FeatureManager.class, NonterminalTransformer.class);
			MLESuffStat stat = constructor.newInstance(fman, transformer);

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
			throw new RuntimeException(e);
		}
	}
}
