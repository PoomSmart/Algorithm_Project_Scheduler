
/**
 * 
 * A modified version of GraphPanel by PoomSmart
 * Original link: https://gist.github.com/roooodcastro/6325153
 * 
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GraphPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private int width = 800;
	private int height = 400;
	private int padding = 25;
	private int labelPadding = 25;
	private boolean customPointColor;
	private Color pointColor;
	private Color gridColor = new Color(200, 200, 200, 200);
	private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
	private int pointWidth = 6;
	private int numberYDivisions;
	private int numberYInterval = 500;
	private int maxYValue;
	private List<List<Integer>> values;
	private int currentMaxIndex = 0;
	private int currentMaxSize = 0;
	private static Random random = new Random();
	private List<List<Color>> custom_point_colors;
	private List<Color> custom_line_colors;
	private Integer maxValue = Integer.MIN_VALUE;
	private boolean everyX = true;
	private int xValueShift = 1;
	private boolean largeData;

	public static double xMultiplier = 1;

	public GraphPanel(List<List<Integer>> values, List<List<Color>> custom_point_colors,
			List<Color> custom_line_colors) {
		this.values = values;
		this.custom_point_colors = custom_point_colors;
		this.custom_line_colors = custom_line_colors;
		customPointColor = this.custom_point_colors != null;
		pointColor = customPointColor ? null : new Color(100, 100, 100, 180);
		for (List<Integer> subvalues : values) {
			if (currentMaxSize < subvalues.size()) {
				currentMaxIndex = values.indexOf(subvalues);
				currentMaxSize = subvalues.size();
			}
		}
		if (getMaxValue() > numberYInterval) {
			maxYValue = getMaxValue() + numberYInterval - (getMaxValue() % numberYInterval);
			largeData = true;
		} else {
			maxYValue = getMaxValue();
			largeData = false;
		}
		numberYDivisions = maxYValue;
	}

	public GraphPanel(List<List<Integer>> values, List<List<Color>> custom_point_colors) {
		this(values, custom_point_colors, null);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int leftPadding = padding + labelPadding;
		int baseY = getHeight() - leftPadding;
		int gridWidth = getWidth() - leftPadding - padding;
		int gridHeight = baseY - padding;

		double xScale = (double) gridWidth / (values.get(currentMaxIndex).size() - 1);
		double yScale = (double) gridHeight / maxYValue;

		int x0, x1, y0, y1;

		List<List<Point>> graphPoints = new Vector<>();

		// Adding points
		for (List<Integer> subscores : values) {
			List<Point> subgraphPoints = new Vector<>();
			for (int i = 0; i < subscores.size(); i++) {
				x1 = (int) (i * xScale + padding + labelPadding);
				y1 = baseY - (int) (subscores.get(i) * yScale);
				subgraphPoints.add(new Point(x1, y1));
			}
			graphPoints.add(subgraphPoints);
		}

		// draw white background
		g2.setColor(Color.WHITE);
		g2.fillRect(leftPadding, padding, gridWidth, gridHeight);

		// create hatch marks and grid lines for y axis.
		List<Integer> yLabels = new Vector<Integer>();
		x0 = padding + labelPadding;
		x1 = x0 + pointWidth;
		for (int i = 0; i < numberYDivisions + 1; ++i) {
			if (!values.isEmpty()) {
				g2.setColor(gridColor);
				int y = ((int) (maxYValue * ((i * 1.0) / numberYDivisions) * 100)) / 100;
				if (!yLabels.contains(y)) {
					if (!largeData || (largeData && i % numberYInterval == 0)) {
						String yLabel = y + "";
						y0 = baseY - (int) (i * yScale);
						g2.drawLine(x1 + 1, y0, getWidth() - padding, y0);
						g2.setColor(Color.BLACK);
						FontMetrics metrics = g2.getFontMetrics();
						int labelWidth = metrics.stringWidth(yLabel);
						g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
						yLabels.add(y);
						g2.drawLine(x0, y0, x1, y0);
					}
				}
			}
		}
		yLabels = null;

		// and for x axis
		y0 = getHeight() - padding - labelPadding;
		y1 = y0 - pointWidth;
		for (int i = 0; i < values.get(currentMaxIndex).size(); i++) {
			if (values.get(currentMaxIndex).size() > 1) {
				x0 = i * (getWidth() - padding * 2 - labelPadding) / (values.get(currentMaxIndex).size() - 1) + padding
						+ labelPadding;
				x1 = x0;
				if (everyX || (i % ((int) ((values.get(currentMaxIndex).size() / 20.0)) + 1)) == 0) {
					g2.setColor(gridColor);
					g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
					g2.setColor(Color.BLACK);
					String xLabel = (int) ((i + xValueShift) * xMultiplier) + "";
					FontMetrics metrics = g2.getFontMetrics();
					int labelWidth = metrics.stringWidth(xLabel);
					g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
				}
				g2.drawLine(x0, y0, x1, y1);
			}
		}

		// create x and y axes
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding,
				getHeight() - padding - labelPadding);

		Stroke oldStroke = g2.getStroke();
		g2.setStroke(GRAPH_STROKE);

		int idx = 0;
		for (List<Point> subgraphPoints : graphPoints) {
			Color color = custom_line_colors != null ? custom_line_colors.get(idx)
					: new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
			g2.setColor(color);
			for (int i = 0; i < subgraphPoints.size() - 1; ++i) {
				x0 = subgraphPoints.get(i).x;
				y0 = subgraphPoints.get(i).y;
				x1 = subgraphPoints.get(i + 1).x;
				y1 = subgraphPoints.get(i + 1).y;
				g2.drawLine(x0, y0, x1, y1);
			}
			++idx;
		}

		g2.setStroke(oldStroke);
		if (pointColor != null)
			g2.setColor(pointColor);
		idx = 0;
		for (List<Point> subgraphPoints : graphPoints) {
			for (int i = 0; i < subgraphPoints.size(); ++i) {
				int x = subgraphPoints.get(i).x - pointWidth / 2;
				int y = subgraphPoints.get(i).y - pointWidth / 2;
				int ovalW = pointWidth;
				int ovalH = pointWidth;
				if (customPointColor)
					g2.setColor(custom_point_colors.get(idx).get(i));
				else
					g2.setColor(pointColor);
				g2.fillOval(x, y, ovalW, ovalH);
			}
			++idx;
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	@SuppressWarnings("unused")
	private Integer getMinValue() {
		Integer minValue = Integer.MAX_VALUE;
		for (List<Integer> subvalues : values) {
			for (Integer value : subvalues)
				minValue = Math.min(minValue, value);
		}
		return minValue;
	}

	private Integer getMaxValue() {
		if (maxValue != Integer.MIN_VALUE)
			return maxValue;
		for (List<Integer> subvalues : values) {
			for (Integer value : subvalues)
				maxValue = Math.max(maxValue, value);
		}
		return maxValue;
	}

	public void setScores(List<List<Integer>> values) {
		this.values = values;
		invalidate();
		this.repaint();
	}

	public List<List<Integer>> getValues() {
		return values;
	}

	public static void _constructGraphs(String name, List<List<Integer>> values, List<List<Color>> point_colors,
			List<Color> line_colors) {
		GraphPanel mainPanel = new GraphPanel(values, point_colors, line_colors);
		JFrame frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(mainPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void constructGraphs(List<List<Integer>> values, List<List<Color>> colors) {
		_constructGraphs(null, values, colors, null);
	}

	public static void constructGraph(String name, Collection<Integer> values, List<Color> point_colors,
			Color line_color) {
		List<List<Integer>> c_values = new Vector<>();
		List<List<Color>> c_point_colors = null;
		List<Color> c_line_colors = null;
		List<Integer> subvalues = new Vector<>();
		List<Color> subpointcolors = new Vector<>();
		for (Integer d : values)
			subvalues.add(d);
		c_values.add(subvalues);
		if (point_colors != null) {
			c_point_colors = new Vector<>();
			for (Color c : point_colors)
				subpointcolors.add(c);
			c_point_colors.add(subpointcolors);
		}
		if (line_color != null) {
			c_line_colors = new Vector<>();
			for (int i = 0; i < values.size(); ++i)
				c_line_colors.add(line_color);
		}
		_constructGraphs(name, c_values, c_point_colors, c_line_colors);
	}
	
	public static void constructGraph(String name, Collection<Integer> values, List<Color> point_colors) {
		constructGraph(name, values, point_colors, null);
	}
	
	public static void constructGraph(String name, Collection<Integer> values) {
		constructGraph(name, values, null);
	}
}