package co.com.neubs.shopneubs.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import co.com.neubs.shopneubs.ProductoDetalleActivity;
import co.com.neubs.shopneubs.R;
import co.com.neubs.shopneubs.classes.APIRest;
import co.com.neubs.shopneubs.classes.APIValidations;
import co.com.neubs.shopneubs.classes.ConsultaPaginada;
import co.com.neubs.shopneubs.classes.Helper;
import co.com.neubs.shopneubs.classes.models.Producto;
import co.com.neubs.shopneubs.classes.models.SaldoInventario;
import co.com.neubs.shopneubs.controls.ImageLoaderView;
import co.com.neubs.shopneubs.interfaces.IServerCallback;

/**
 * Created by bikerlfh on 6/22/17.
 */

public class ProductoSectionItemAdapter extends RecyclerView.Adapter<ProductoSectionItemAdapter.ProductoSectionItemViewHolder> {
    private final String TAG = "ProductoAdapter";
    private ArrayList<SaldoInventario> listado_saldo_inventario;
    private ConsultaPaginada consultaPaginada;
    private String nextPage;
    private Context context;

    // The class viewHolder
    public static class ProductoSectionItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageLoaderView imagen;
        private TextView nombre_producto;
        private TextView precio;
        private TextView oferta;

        private SaldoInventario saldoInventario;

        private Context context;

        public ProductoSectionItemViewHolder(View itemView) {

            super(itemView);
            imagen = (ImageLoaderView)itemView.findViewById(R.id.img_card_section_item);
            nombre_producto = (TextView)itemView.findViewById(R.id.lbl_nombre_producto_card_section_item);
            precio = (TextView)itemView.findViewById(R.id.lbl_precio_producto_card_section_item);
            oferta = (TextView)itemView.findViewById(R.id.lbl_precio_anterior_card_section_item);
            //Estilo Texto strikethrough
            oferta.setPaintFlags(precio.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            context = itemView.getContext();

        }

        public void bindProducto(SaldoInventario saldoInventario) {
            this.saldoInventario = saldoInventario;
            Producto producto = saldoInventario.getProducto();
            nombre_producto.setText(producto.getNombre());



            if (saldoInventario.getPrecioOferta() > 0){
                oferta.setText(Helper.MoneyFormat(saldoInventario.getPrecioVentaUnitario()));
                precio.setText(Helper.MoneyFormat(saldoInventario.getPrecioOferta()));
            }
            else{
                precio.setText(Helper.MoneyFormat(saldoInventario.getPrecioVentaUnitario()));
                oferta.setVisibility(View.GONE);
            }
            // Se obtiene la imagen y se guarda en el cache
            imagen.setImageURL(producto.getImagen());

            imagen.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Aca se debe abrir la actividad de visualización del producto detalle
            Intent intent = new Intent(v.getContext(),ProductoDetalleActivity.class);
            intent.putExtra(ProductoDetalleActivity.PARAM_ID_SALDO_INVENTARIO,this.saldoInventario.getIdSaldoInventario());
            intent.putExtra(ProductoDetalleActivity.PARAM_NOMBRE_PRODUCTO,this.saldoInventario.getProducto().getNombre());
            intent.putExtra(ProductoDetalleActivity.PARAM_PRECIO_OFERTA,this.saldoInventario.getPrecioOferta());
            intent.putExtra(ProductoDetalleActivity.PARAM_PRECIO_VENTA_UNITARIO,this.saldoInventario.getPrecioVentaUnitario());
            intent.putExtra(ProductoDetalleActivity.PARAM_ESTADO,this.saldoInventario.getEstado());
            v.getContext().startActivity(intent);
        }
    }

    public  ProductoSectionItemAdapter(Context context, ConsultaPaginada consultaPaginada){
        this.context = context;
        this.consultaPaginada = consultaPaginada;
        this.listado_saldo_inventario = this.consultaPaginada.getResults();
        this.nextPage = this.consultaPaginada.getNext();
    }

    @Override
    public ProductoSectionItemViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        // inflate layout CartView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_section_item,null);
        return new ProductoSectionItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductoSectionItemViewHolder holder, final int position) {
        SaldoInventario saldoInventario = listado_saldo_inventario.get(position);
        holder.bindProducto(saldoInventario);
    }

    @Override
    public int getItemCount() {
        return listado_saldo_inventario.size();
    }

    private void addItems(int position,ArrayList<SaldoInventario> listadoSaldoInventario){
        listado_saldo_inventario.addAll(listadoSaldoInventario);
        notifyItemRangeInserted(position,listadoSaldoInventario.size());
    }

    /**
     * Consulta la siguiente pagina si existe
     * Este metodo debe ser llamado desde el evento OnScrollListener (onScrolledToBottom)
     */
    public void getNextPage(View view){
        if (this.nextPage!= null && this.nextPage.length() > 0){

            final Snackbar snackbar = Snackbar.make(view, R.string.text_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null);
            snackbar.show();
            final ProgressBar spinner = (ProgressBar) view.findViewById(R.id.progress_bar);
            spinner.setVisibility(View.VISIBLE);
            // Se consulta a la api la proxima pagina
            APIRest.Async.get(this.nextPage, new IServerCallback() {
                @Override
                public void onSuccess(String json) {
                    ConsultaPaginada cPaginada = APIRest.serializeObjectFromJson(json,ConsultaPaginada.class);
                    addItems(getItemCount(),cPaginada.getResults());
                    // Se modifica nextPage asignandole la nueva (pagina siguiente)
                    nextPage = cPaginada.getNext();
                    snackbar.dismiss();
                    spinner.setVisibility(View.GONE);
                }

                @Override
                public void onError(String message_error, APIValidations apiValidations) {
                    Log.d(TAG,"ERROR: " + message_error);
                    snackbar.dismiss();
                    spinner.setVisibility(View.GONE);
                }
            });
        }
    }
}