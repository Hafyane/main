

<div class="span-18 last">
  <h1>{translate text="Material"}</h1>
<h2>Material Cosechado por Fecha</h2> 
{literal}
      <style type="text/css">
 
	        #container3 {
        width : 600px;
        height: 384px;
        margin: 8px auto;
      }
    </style>
{/literal}
<div id="container3"> </div>
    	{* Load graphic library *}{js filename="flotr2.min.js"}
{literal}


<script type="text/javascript">
(
function basic_time(container) {
{/literal} 
{$output9}
{literal}
    options = {
        xaxis: {
            title: 'Fecha',
            labelsAngle: 0,
		noTicks: 17,
		mode:"time",
		timeformat: "%y"
        },
        yaxis: {
            title: 'Registros',
        },	
        selection: {
            mode: 'x'
        },
        HtmlText: false,
        title: 'Registros por Fecha'
    };

    // Draw graph with default options, overwriting with passed options


    function drawGraph(opts) {

        // Clone the options, so the 'options' variable always keeps intact.
        o = Flotr._.extend(Flotr._.clone(options), opts || {});

        // Return a new graph.
        return Flotr.draw(
        container, [d1], o);
    }

    graph = drawGraph();

  })
( container=document.getElementById("container3")
);
    </script>
{/literal} 
<div class="span-20" >
{$output10}
</div>
<div class="clear"></div>
