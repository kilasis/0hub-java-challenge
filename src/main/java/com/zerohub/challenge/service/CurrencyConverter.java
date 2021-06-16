package com.zerohub.challenge.service;

import com.zerohub.challenge.exception.UnsupportedCurrencyException;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.graph.concurrent.AsSynchronizedGraph;
import org.jgrapht.util.SupplierUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Currency converter
 *
 * @implNote A directed unweighted/weighted graph is used to store and manage rates.
 * If you need to calculate minimum conversion rate, you can make graph weighted by using property <code>conversion-rate.calculate-min<code/>,
 * but do it carefully, because library uses double type for weights.
 * For now, minimum number of conversions is preferred and unweighted graph is default
 */
@Component
public class CurrencyConverter {
  private static final int RATE_REVERSE_SCALE = 10;
  private static final int PRICE_SCALE = 4;
  private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

  private final boolean weightedGraph;
  private final Graph<String, BigDecimal> graph;
  private final DijkstraShortestPath<String, BigDecimal> dijkstraShortestPath;

  public CurrencyConverter(@Value("${conversion-rate.calculate-min:false}") boolean calculateMinConversionRate) {
    this.weightedGraph = calculateMinConversionRate;
    this.graph = new AsSynchronizedGraph<>(GraphTypeBuilder
        .directed()
        .weighted(weightedGraph)
        .allowingMultipleEdges(false)
        .allowingSelfLoops(false)
        .vertexSupplier(SupplierUtil.createStringSupplier())
        .edgeSupplier(() -> BigDecimal.ONE)
        .buildGraph());

    this.dijkstraShortestPath = new DijkstraShortestPath<>(graph);
  }

  /**
   * Adds rate for direct and reverse conversion from <code>baseCurrency<code/> to <code>quoteCurrency<code/>
   *
   * @param baseCurrency  base currency for <code>rate<code/>
   * @param quoteCurrency quote currency for <code>rate<code/>
   * @param rate          rate for currency conversion
   * @implNote creates two vertices and two edges in the graph, one edge contains a direct rate, other contains a reverse rate.
   */
  public void addRate(String baseCurrency, String quoteCurrency, BigDecimal rate) {
    BigDecimal reversedRate = BigDecimal.ONE.divide(rate, RATE_REVERSE_SCALE, ROUNDING_MODE);

    graph.addVertex(baseCurrency);
    graph.addVertex(quoteCurrency);
    graph.addEdge(baseCurrency, quoteCurrency, rate);
    graph.addEdge(quoteCurrency, baseCurrency, reversedRate);

    if (weightedGraph) {
      graph.setEdgeWeight(rate, rate.doubleValue());
      graph.setEdgeWeight(reversedRate, reversedRate.doubleValue());
    }
  }

  /**
   * Converts <code>amount<code/> from one currency to other currency
   *
   * @param fromCurrency currency of received <code>amount<code/>
   * @param toCurrency   currency of expected conversion result
   * @param amount       amount which should be converted
   * @return converted <code>amount<code/>
   */
  public BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount) {
    if (!graph.containsVertex(fromCurrency) || !graph.containsVertex(toCurrency)) {
      throw new UnsupportedCurrencyException();
    }

    GraphPath<String, BigDecimal> path = dijkstraShortestPath.getPath(fromCurrency, toCurrency);

    List<BigDecimal> edges = path.getEdgeList();

    return edges.stream()
        .reduce(amount, BigDecimal::multiply)
        .setScale(PRICE_SCALE, ROUNDING_MODE);
  }
}
