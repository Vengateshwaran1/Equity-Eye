import { useState} from 'react';
import axios from 'axios';
import './Stock.css';
import Plot from 'react-plotly.js';


const Stock = () => {
  const [symbol, setSymbol] = useState('');
  const [stockChartXValues, setStockChartXValues] = useState([]);
  const [stockChartYValues, setStockChartYValues] = useState([]);
  const [error, setError] = useState(null); 

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!symbol) return; 

    try {
      const response = await axios.get(`/api/stock/${symbol}`);
       console.log(response.data)
      const data = response.data;

      const timeSeriesDaily = data.timeSeriesDaily;
      if (!timeSeriesDaily) {
        throw new Error("Invalid response format from backend");
      }

      setStockChartXValues(timeSeriesDaily.map(item => item.date));
      setStockChartYValues(timeSeriesDaily.map(item => item.open));
      setError(null); 
    } catch (error) {
      console.error(error);
      setError(error.message); 
    }
  };

  return (
    <div className='container'>
      <h1>Stock Market</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="symbol">Stock Symbol:</label>
        <input
          type="text"
          id="symbol"
          value={symbol}
          onChange={(e) => setSymbol(e.target.value)}
        />
        <button type="submit">Get Stock Data</button>
      </form>
      {error && <p className="error-message">{error}</p>} 
      <div className='plot'>
        {stockChartXValues.length > 0 && (
          <Plot
            data={[
              {
                x: stockChartXValues,
                y: stockChartYValues,
                type: 'scatter',
                mode: 'lines+markers',
                marker: { color: 'red' },
              },
            ]}
            onHover='hello'
            layout={{ width: 720, height: 440, title: 'A Fancy Plot' }}
          />
        )}
      </div>
    </div>
  );
};

export default Stock;

