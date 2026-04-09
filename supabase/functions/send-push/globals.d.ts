declare module "@supabase/supabase-js" {
  export function createClient(url: string, key: string, options?: any): any;
}

declare module "https://esm.sh/@supabase/supabase-js@2" {
  export function createClient(url: string, key: string, options?: any): any;
}

declare const Deno: {
  serve: (handler: (req: Request) => Response | Promise<Response>) => unknown;
  env: {
    get: (key: string) => string | undefined;
  };
};
